package com.example.iot.vendor;

import com.example.iot.domain.Appliance;
import org.springframework.stereotype.Component;
import java.time.Instant;
import java.util.List;

/** Provides deterministic metrics in place of real vendor API integrations. */
@Component
public class MockVendorMetricClient implements VendorMetricClient {
    /** Returns appliance-type-specific mock metrics with a small deterministic variation. */
    @Override public List<VendorMetric> fetchMetrics(Appliance appliance, Instant collectedAt) {
        double variation = Math.abs((appliance.getId() * 31 + collectedAt.getEpochSecond()) % 10);
        return switch (appliance.getType().toLowerCase()) {
            case "refrigerator", "fridge" -> List.of(new VendorMetric("temperature", 2 + variation / 10, "C"), new VendorMetric("door_open", variation % 2, "boolean"));
            case "air_conditioner", "ac" -> List.of(new VendorMetric("temperature", 19 + variation / 10, "C"), new VendorMetric("power", 450 + variation * 5, "W"));
            default -> List.of(new VendorMetric("power", 100 + variation * 10, "W"), new VendorMetric("status", variation % 2, "boolean"));
        };
    }
}