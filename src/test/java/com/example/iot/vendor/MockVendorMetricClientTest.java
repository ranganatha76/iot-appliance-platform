package com.example.iot.vendor;

import com.example.iot.domain.Appliance;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import java.time.Instant;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

/** Verifies the appliance-specific metric profiles exposed by the vendor mock. */
class MockVendorMetricClientTest {
    private final MockVendorMetricClient client = new MockVendorMetricClient();

    /** Verifies that each added appliance profile returns its documented metric names. */
    @Test void addedApplianceTypesReturnSpecificMetrics() {
        assertMetricNames("television", "power", "volume");
        assertMetricNames("oven", "temperature", "door_open");
        assertMetricNames("washer", "cycle_progress", "water_usage");
        assertMetricNames("dryer", "cycle_progress", "humidity");
    }

    /** Collects one mock profile and verifies its metric names in their returned order. */
    private void assertMetricNames(String applianceType, String... expectedMetricNames) {
        Appliance appliance = new Appliance("Test appliance", applianceType, "acme", 60);
        ReflectionTestUtils.setField(appliance, "id", 1L);
        List<VendorMetric> metrics = client.fetchMetrics(appliance, Instant.parse("2026-08-18T00:00:00Z"));

        assertThat(metrics).extracting(VendorMetric::name).containsExactly(expectedMetricNames);
    }
}