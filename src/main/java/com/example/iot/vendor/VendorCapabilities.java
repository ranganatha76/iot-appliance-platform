package com.example.iot.vendor;

import java.util.Set;

/** Describes the appliance types and normalized metric names supplied by one vendor. */
public record VendorCapabilities(Set<String> applianceTypes, Set<String> metricNames) {
    /** Returns whether the vendor supports an appliance type or its wildcard capability. */
    public boolean supportsApplianceType(String applianceType) { return applianceTypes.contains("*") || applianceTypes.contains(applianceType.toLowerCase()); }
}