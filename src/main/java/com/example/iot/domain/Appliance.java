package com.example.iot.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
public class Appliance {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false) private String name;
    @Column(nullable = false) private String type;
    @Column(nullable = false) private String vendor;
    @Column(nullable = false) private int collectionIntervalSeconds;
    @Column(nullable = false) private boolean enabled = true;
    private Instant lastCollectedAt;

    /** Creates an empty entity instance for JPA hydration. */
    protected Appliance() { }

    /** Creates a managed appliance with its collection configuration. */
    public Appliance(String name, String type, String vendor, int collectionIntervalSeconds) { this.name = name; this.type = type; this.vendor = vendor; this.collectionIntervalSeconds = collectionIntervalSeconds; }

    /** Returns the persistent appliance identifier. */
    public Long getId() { return id; }

    /** Returns the appliance's client-visible name. */
    public String getName() { return name; }

    /** Returns the appliance category used to select mock vendor metrics. */
    public String getType() { return type; }

    /** Returns the vendor identifier associated with the appliance. */
    public String getVendor() { return vendor; }

    /** Returns the minimum interval between automatic metric collections. */
    public int getCollectionIntervalSeconds() { return collectionIntervalSeconds; }

    /** Returns whether automatic and manual collection are permitted. */
    public boolean isEnabled() { return enabled; }

    /** Returns when metrics were last collected, or {@code null} before first collection. */
    public Instant getLastCollectedAt() { return lastCollectedAt; }

    /** Replaces the appliance details and collection configuration. */
    public void update(String name, String type, String vendor, int interval, boolean enabled) { this.name = name; this.type = type; this.vendor = vendor; this.collectionIntervalSeconds = interval; this.enabled = enabled; }

    /** Records the successful collection time used to calculate the next due interval. */
    public void markCollected(Instant timestamp) { this.lastCollectedAt = timestamp; }
}