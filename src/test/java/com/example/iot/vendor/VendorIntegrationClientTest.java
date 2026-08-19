package com.example.iot.vendor;

import com.example.iot.domain.Appliance;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import java.time.Instant;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Verifies the distinct contracts modeled by the named vendor adapters. */
class VendorIntegrationClientTest {
    private static final Instant COLLECTION_TIME = Instant.parse("2026-08-19T00:00:00Z");

    /** Verifies ACME selection, supported capabilities, and REST-field normalization. */
    @Test void acmeUsesBearerAuthenticationAndNormalizesRestFields() {
        AcmeRestVendorMetricClient client = new AcmeRestVendorMetricClient("token");

        assertThat(client.supports("acme")).isTrue();
        assertThat(client.capabilities().supportsApplianceType("refrigerator")).isTrue();
        assertThat(client.fetchMetrics(appliance("refrigerator"), COLLECTION_TIME)).extracting(VendorMetric::name).containsExactlyInAnyOrder("temperature", "power");
        assertThatThrownBy(() -> new AcmeRestVendorMetricClient("").fetchMetrics(appliance("refrigerator"), COLLECTION_TIME)).isInstanceOf(VendorIntegrationException.class).hasMessageContaining("authentication");
    }

    /** Verifies Northwind GraphQL authentication, capabilities, rate limiting, and temporary failures. */
    @Test void northwindEnforcesCapabilitiesRateLimitsAndReliabilityFailures() {
        NorthwindGraphQlVendorMetricClient client = new NorthwindGraphQlVendorMetricClient("key", 1, 0);

        assertThat(client.supports("northwind")).isTrue();
        assertThat(client.capabilities().supportsApplianceType("washer")).isTrue();
        assertThat(client.fetchMetrics(appliance("washer"), COLLECTION_TIME)).extracting(VendorMetric::name).containsExactlyInAnyOrder("cycle_progress", "power");
        assertThatThrownBy(() -> client.fetchMetrics(appliance("washer"), COLLECTION_TIME)).isInstanceOf(VendorIntegrationException.class).hasMessageContaining("rate limit");
        NorthwindGraphQlVendorMetricClient unreliableClient = new NorthwindGraphQlVendorMetricClient("key", 10, 2);
        unreliableClient.fetchMetrics(appliance("dryer"), COLLECTION_TIME);
        assertThatThrownBy(() -> unreliableClient.fetchMetrics(appliance("dryer"), COLLECTION_TIME)).isInstanceOf(VendorIntegrationException.class).hasMessageContaining("temporarily unavailable");
    }

    /** Creates an appliance with an assigned ID for deterministic mock-vendor variation. */
    private Appliance appliance(String type) {
        Appliance appliance = new Appliance("Test appliance", type, "test", 60);
        ReflectionTestUtils.setField(appliance, "id", 1L);
        return appliance;
    }
}