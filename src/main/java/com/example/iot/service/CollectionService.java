package com.example.iot.service;

import com.example.iot.domain.Appliance;
import com.example.iot.domain.MetricObservation;
import com.example.iot.repository.ApplianceRepository;
import com.example.iot.repository.MetricObservationRepository;
import com.example.iot.vendor.VendorMetric;
import com.example.iot.vendor.VendorMetricClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Duration;
import java.time.Instant;

@Service
public class CollectionService {
    private final ApplianceRepository appliances;
    private final MetricObservationRepository metrics;
    private final VendorMetricClient vendorMetricClient;

    /** Creates the collection coordinator with appliance and historical-metric storage. */
    public CollectionService(ApplianceRepository appliances, MetricObservationRepository metrics, VendorMetricClient vendorMetricClient) { this.appliances = appliances; this.metrics = metrics; this.vendorMetricClient = vendorMetricClient; }

    /** Collects only enabled appliances whose configured polling interval has elapsed. */
    @Scheduled(fixedDelayString = "${collection.scheduler-delay-ms:5000}") public void collectDueAppliances() { collect(false); }

    /** Collects vendor samples and persists them; forcing bypasses only the interval check. */
    @Transactional public CollectionResult collect(boolean force) {
        Instant now = Instant.now(); int collected = 0; int stored = 0;
        for (Appliance appliance : appliances.findAll()) {
            boolean due = appliance.getLastCollectedAt() == null || Duration.between(appliance.getLastCollectedAt(), now).getSeconds() >= appliance.getCollectionIntervalSeconds();
            if (!appliance.isEnabled() || (!force && !due)) continue;
            for (VendorMetric sample : vendorMetricClient.fetchMetrics(appliance, now)) {
                metrics.save(new MetricObservation(appliance, now, sample.name(), sample.value(), sample.unit()));
                stored++;
            }
            appliance.markCollected(now); collected++;
        }
        return new CollectionResult(collected, stored);
    }

    public record CollectionResult(int appliancesCollected, int metricsStored) { }
}