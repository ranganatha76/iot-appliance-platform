package com.example.iot.service;

import com.example.iot.domain.MetricObservation;
import com.example.iot.repository.MetricObservationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;

@Service
public class MetricService {
    private final MetricObservationRepository repository;

    /** Creates the metric query service with its historical storage dependency. */
    public MetricService(MetricObservationRepository repository) { this.repository = repository; }

    /** Returns raw metric observations in the requested half-open time range. */
    @Transactional(readOnly = true) public List<MetricObservation> findBetween(Instant start, Instant end) {
        if (!start.isBefore(end)) throw new IllegalArgumentException("start must be before end");
        return repository.findByCollectedAtGreaterThanEqualAndCollectedAtLessThan(start, end);
    }
}