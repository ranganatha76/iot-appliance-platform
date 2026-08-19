package com.example.iot.vendor;

import com.example.iot.domain.Appliance;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/** Simulates Northwind's API-key-authenticated GraphQL API, limits, and transient failures. */
@Component
public class NorthwindGraphQlVendorMetricClient implements VendorMetricClient {
    private final String apiKey;
    private final int maxRequestsPerMinute;
    private final int failEveryRequests;
    private final AtomicInteger requestsInWindow = new AtomicInteger();
    private final AtomicInteger requests = new AtomicInteger();
    private Instant windowStart = Instant.EPOCH;

    /** Creates the Northwind adapter with credentials, rate-limit, and failure-simulation settings. */
    public NorthwindGraphQlVendorMetricClient(@Value("${vendors.northwind.api-key}") String apiKey, @Value("${vendors.northwind.max-requests-per-minute}") int maxRequestsPerMinute, @Value("${vendors.northwind.fail-every-requests}") int failEveryRequests) { this.apiKey = apiKey; this.maxRequestsPerMinute = maxRequestsPerMinute; this.failEveryRequests = failEveryRequests; }

    /** Selects this GraphQL adapter for Northwind appliances. */
    @Override public boolean supports(String vendor) { return "northwind".equalsIgnoreCase(vendor); }

    /** Declares the Northwind appliance and normalized metric capability set. */
    @Override public VendorCapabilities capabilities() { return new VendorCapabilities(Set.of("washer", "washing_machine", "dryer", "television", "tv"), Set.of("cycle_progress", "power")); }

    /** Authenticates, enforces the vendor limit, simulates reliability, and normalizes GraphQL data. */
    @Override public synchronized List<VendorMetric> fetchMetrics(Appliance appliance, Instant collectedAt) {
        if (apiKey.isBlank()) throw new VendorIntegrationException("Northwind authentication failed: API key is not configured");
        enforceRateLimit(collectedAt);
        if (failEveryRequests > 0 && requests.incrementAndGet() % failEveryRequests == 0) throw new VendorIntegrationException("Northwind API temporarily unavailable");
        Map<String, Double> payload = graphQlPayload(appliance, collectedAt);
        return payload.entrySet().stream().map(metric -> new VendorMetric(metric.getKey().equals("wattsNow") ? "power" : metric.getKey().equals("completion") ? "cycle_progress" : metric.getKey(), metric.getValue(), metric.getKey().equals("wattsNow") ? "W" : "percent")).toList();
    }

    /** Rejects calls above Northwind's configured rolling one-minute API allowance. */
    private void enforceRateLimit(Instant collectedAt) {
        if (windowStart.plus(1, ChronoUnit.MINUTES).isBefore(collectedAt)) { windowStart = collectedAt; requestsInWindow.set(0); }
        if (requestsInWindow.incrementAndGet() > maxRequestsPerMinute) throw new VendorIntegrationException("Northwind API rate limit exceeded");
    }

    /** Represents a GraphQL response shape whose field names differ from the platform model. */
    private Map<String, Double> graphQlPayload(Appliance appliance, Instant collectedAt) {
        double variation = Math.abs((appliance.getId() * 23 + collectedAt.getEpochSecond()) % 10);
        return "washer".equalsIgnoreCase(appliance.getType()) || "dryer".equalsIgnoreCase(appliance.getType())
                ? Map.of("completion", variation * 10, "wattsNow", 300 + variation * 8)
                : Map.of("completion", variation * 10, "wattsNow", 70 + variation * 4);
    }
}