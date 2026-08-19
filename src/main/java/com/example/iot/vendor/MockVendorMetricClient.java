package com.example.iot.vendor;

import com.example.iot.domain.Appliance;
import org.springframework.stereotype.Component;
import java.time.Instant;
import java.util.List;
import java.util.Set;

/** Provides deterministic metrics in place of real vendor API integrations. */
@Component
public class MockVendorMetricClient implements VendorMetricClient {
    /** Handles unrecognized vendors with generic deterministic metrics for local review. */
    @Override public boolean supports(String vendor) { return !"acme".equalsIgnoreCase(vendor) && !"northwind".equalsIgnoreCase(vendor); }

    /** Declares generic support for appliance types not owned by a named vendor adapter. */
    @Override public VendorCapabilities capabilities() { return new VendorCapabilities(Set.of("*"), Set.of("power", "status", "temperature", "door_open", "volume", "cycle_progress", "water_usage", "humidity")); }

    /** Returns appliance-type-specific mock metrics with a small deterministic variation. */
    @Override public List<VendorMetric> fetchMetrics(Appliance appliance, Instant collectedAt) {
        double variation = Math.abs((appliance.getId() * 31 + collectedAt.getEpochSecond()) % 10);
        return switch (appliance.getType().toLowerCase()) {
            case "refrigerator", "fridge" -> List.of(new VendorMetric("temperature", 2 + variation / 10, "C"), new VendorMetric("door_open", variation % 2, "boolean"));
            case "air_conditioner", "ac" -> List.of(new VendorMetric("temperature", 19 + variation / 10, "C"), new VendorMetric("power", 450 + variation * 5, "W"));
            case "television", "tv" -> List.of(new VendorMetric("power", 80 + variation * 4, "W"), new VendorMetric("volume", 20 + variation * 5, "percent"));
            case "oven" -> List.of(new VendorMetric("temperature", 175 + variation * 5, "C"), new VendorMetric("door_open", variation % 2, "boolean"));
            case "washer", "washing_machine" -> List.of(new VendorMetric("cycle_progress", variation * 10, "percent"), new VendorMetric("water_usage", 30 + variation * 2, "L"));
            case "dryer" -> List.of(new VendorMetric("cycle_progress", variation * 10, "percent"), new VendorMetric("humidity", 10 + variation * 3, "percent"));
            default -> List.of(new VendorMetric("power", 100 + variation * 10, "W"), new VendorMetric("status", variation % 2, "boolean"));
        };
    }
}