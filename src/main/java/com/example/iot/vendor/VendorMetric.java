package com.example.iot.vendor;

/** Represents a vendor metric after it has been normalized for persistence. */
public record VendorMetric(String name, double value, String unit) { }