package com.example.iot.vendor;

import com.example.iot.domain.Appliance;
import java.time.Instant;
import java.util.List;

/** Supplies normalized metrics for an appliance from its external vendor. */
public interface VendorMetricClient {
    /** Fetches the metrics observed for an appliance at the supplied instant. */
    List<VendorMetric> fetchMetrics(Appliance appliance, Instant collectedAt);
}