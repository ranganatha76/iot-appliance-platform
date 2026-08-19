package com.example.iot.vendor;

import com.example.iot.domain.Appliance;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Simulates ACME's bearer-authenticated REST API and normalizes its JSON field names. */
@Component
public class AcmeRestVendorMetricClient implements VendorMetricClient {
    private final String bearerToken;

    /** Creates the ACME adapter with its externally configured bearer credential. */
    public AcmeRestVendorMetricClient(@Value("${vendors.acme.bearer-token}") String bearerToken) { this.bearerToken = bearerToken; }

    /** Selects this REST adapter for ACME appliances. */
    @Override public boolean supports(String vendor) { return "acme".equalsIgnoreCase(vendor); }

    /** Declares the ACME appliance and normalized metric capability set. */
    @Override public VendorCapabilities capabilities() { return new VendorCapabilities(Set.of("refrigerator", "fridge", "air_conditioner", "ac"), Set.of("temperature", "power")); }

    /** Authenticates, reads an ACME-style REST payload, and maps it to normalized metrics. */
    @Override public List<VendorMetric> fetchMetrics(Appliance appliance, Instant collectedAt) {
        if (bearerToken.isBlank()) throw new VendorIntegrationException("ACME authentication failed: bearer token is not configured");
        Map<String, Double> payload = restPayload(appliance, collectedAt);
        return payload.entrySet().stream().map(metric -> new VendorMetric(metric.getKey().equals("watts") ? "power" : "temperature", metric.getValue(), metric.getKey().equals("watts") ? "W" : "C")).toList();
    }

    /** Represents the ACME REST response shape before field normalization. */
    private Map<String, Double> restPayload(Appliance appliance, Instant collectedAt) {
        double variation = Math.abs((appliance.getId() * 17 + collectedAt.getEpochSecond()) % 10);
        return "refrigerator".equalsIgnoreCase(appliance.getType()) || "fridge".equalsIgnoreCase(appliance.getType())
                ? Map.of("temp_celsius", 2 + variation / 10, "watts", 90 + variation * 2)
                : Map.of("temp_celsius", 20 + variation / 10, "watts", 120 + variation * 3);
    }
}