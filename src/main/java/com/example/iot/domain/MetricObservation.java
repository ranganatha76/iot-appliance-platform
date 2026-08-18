package com.example.iot.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
public class MetricObservation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY) private Appliance appliance;
    @Column(nullable = false) private Instant collectedAt;
    @Column(nullable = false) private String metricName;
    @Column(nullable = false) private double metricValue;
    @Column(nullable = false) private String unit;

    /** Creates an empty entity instance for JPA hydration. */
    protected MetricObservation() { }

    /** Stores one normalized metric sample collected from an appliance. */
    public MetricObservation(Appliance appliance, Instant collectedAt, String metricName, double metricValue, String unit) { this.appliance = appliance; this.collectedAt = collectedAt; this.metricName = metricName; this.metricValue = metricValue; this.unit = unit; }

    /** Returns the appliance that produced this sample. */
    public Appliance getAppliance() { return appliance; }

    /** Returns the instant at which this metric was collected. */
    public Instant getCollectedAt() { return collectedAt; }

    /** Returns the normalized metric name. */
    public String getMetricName() { return metricName; }

    /** Returns the numeric metric value. */
    public double getMetricValue() { return metricValue; }

    /** Returns the measurement unit associated with the value. */
    public String getUnit() { return unit; }
}