package com.example.iot.vendor;

/** Indicates a vendor authentication, rate-limit, or temporary availability failure. */
public class VendorIntegrationException extends RuntimeException {
    /** Creates an integration failure with a client-safe explanatory message. */
    public VendorIntegrationException(String message) { super(message); }
}