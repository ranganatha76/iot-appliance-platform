package com.example.iot.service;

import com.example.iot.api.ApiModels.MetricSummary;
import com.example.iot.api.ApiModels.ReportResponse;
import com.example.iot.domain.MetricObservation;
import com.example.iot.repository.MetricObservationRepository;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.*;

@Service
public class ReportService {
    private final MetricObservationRepository repository;

    /** Creates the report service with access to historical metric observations. */
    public ReportService(MetricObservationRepository repository) { this.repository = repository; }

    /** Aggregates every metric in a validated half-open date range by appliance and metric. */
    public ReportResponse generate(Instant start, Instant end) {
        if (!start.isBefore(end)) throw new IllegalArgumentException("start must be before end");
        Map<String, List<MetricObservation>> groups = new LinkedHashMap<>();
        for (MetricObservation metric : repository.findByCollectedAtGreaterThanEqualAndCollectedAtLessThan(start, end)) groups.computeIfAbsent(metric.getAppliance().getId() + ":" + metric.getMetricName() + ":" + metric.getUnit(), ignored -> new ArrayList<>()).add(metric);
        return new ReportResponse(start, end, groups.values().stream().map(this::summarize).toList());
    }

    /** Converts one appliance-and-metric sample group into its statistical report row. */
    private MetricSummary summarize(List<MetricObservation> values) {
        MetricObservation first = values.getFirst(); DoubleSummaryStatistics stats = values.stream().mapToDouble(MetricObservation::getMetricValue).summaryStatistics();
        return new MetricSummary(first.getAppliance().getId(), first.getAppliance().getName(), first.getMetricName(), first.getUnit(), stats.getCount(), stats.getMin(), stats.getMax(), stats.getAverage());
    }
}