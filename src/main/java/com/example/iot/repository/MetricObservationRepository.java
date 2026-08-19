package com.example.iot.repository;

import com.example.iot.domain.MetricObservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.util.List;

public interface MetricObservationRepository extends JpaRepository<MetricObservation, Long> {
    /** Returns samples in the half-open time range {@code [start, end)} for reporting. */
    @Query("select metric from MetricObservation metric join fetch metric.appliance where metric.collectedAt >= :start and metric.collectedAt < :end")
    List<MetricObservation> findByCollectedAtGreaterThanEqualAndCollectedAtLessThan(@Param("start") Instant start, @Param("end") Instant end);
}