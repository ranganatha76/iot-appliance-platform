package com.example.iot.api;

import com.example.iot.domain.Appliance;
import com.example.iot.domain.MetricObservation;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.List;

public final class ApiModels {
    /** Prevents construction of this namespace for API request and response records. */
    private ApiModels() { }
    public record ApplianceRequest(@NotBlank String name, @NotBlank String type, @NotBlank String vendor, @Min(1) int collectionIntervalSeconds, Boolean enabled) { }
    public record ApplianceResponse(Long id, String name, String type, String vendor, int collectionIntervalSeconds, boolean enabled, Instant lastCollectedAt) {
        /** Maps the persisted appliance entity to its public API representation. */
        public static ApplianceResponse from(Appliance appliance) { return new ApplianceResponse(appliance.getId(), appliance.getName(), appliance.getType(), appliance.getVendor(), appliance.getCollectionIntervalSeconds(), appliance.isEnabled(), appliance.getLastCollectedAt()); }
    }
    public record MetricResponse(Long applianceId, Instant collectedAt, String metricName, double value, String unit) {
        /** Maps one persisted metric sample to its public API representation. */
        public static MetricResponse from(MetricObservation metric) { return new MetricResponse(metric.getAppliance().getId(), metric.getCollectedAt(), metric.getMetricName(), metric.getMetricValue(), metric.getUnit()); }
    }
    public record CollectionResponse(int appliancesCollected, int metricsStored, int failedCollections) { }
    public record MetricSummary(Long applianceId, String applianceName, String metricName, String unit, long samples, double minimum, double maximum, double average) { }
    public record ReportResponse(Instant start, Instant end, List<MetricSummary> metrics) { }
}