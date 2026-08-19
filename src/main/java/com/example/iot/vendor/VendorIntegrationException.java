package com.example.iot.vendor;

/** Indicates a vendor authentication, rate-limit, or temporary availability failure. */
public class VendorIntegrationException extends RuntimeException {
    /** Classifies failures so collection can distinguish vendor errors from persistence faults. */
    public enum FailureType { AUTHENTICATION, CAPABILITY, RATE_LIMIT, TEMPORARY_UNAVAILABLE }

    private final FailureType failureType;

    /** Creates an integration failure with a client-safe explanatory message. */
    public VendorIntegrationException(FailureType failureType, String message) { super(message); this.failureType = failureType; }

    /** Returns the vendor failure category for structured operational logging. */
    public FailureType getFailureType() { return failureType; }
}