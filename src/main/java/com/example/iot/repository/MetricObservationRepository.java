package com.example.iot.repository;

import com.example.iot.domain.MetricObservation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.Instant;
import java.util.List;

public interface MetricObservationRepository extends JpaRepository<MetricObservation, Long> {
    /** Returns samples in the half-open time range {@code [start, end)} for reporting. */
    List<MetricObservation> findByCollectedAtGreaterThanEqualAndCollectedAtLessThan(Instant start, Instant end);
}