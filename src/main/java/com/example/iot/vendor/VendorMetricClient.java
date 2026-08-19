package com.example.iot.vendor;

import com.example.iot.domain.Appliance;
import java.time.Instant;
import java.util.List;

/** Supplies normalized metrics from one external-vendor integration. */
public interface VendorMetricClient {
    /** Reports whether this client owns the supplied vendor identifier. */
    boolean supports(String vendor);

    /** Lists the appliance types and normalized metrics this vendor integration supports. */
    VendorCapabilities capabilities();

    /** Fetches and normalizes metrics observed for an appliance at the supplied instant. */
    List<VendorMetric> fetchMetrics(Appliance appliance, Instant collectedAt);
}