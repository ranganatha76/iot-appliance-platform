package com.example.iot.api;

import com.example.iot.api.ApiModels.*;
import com.example.iot.service.ApplianceService;
import com.example.iot.service.CollectionService;
import com.example.iot.service.MetricService;
import com.example.iot.service.ReportService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.time.*;
import java.util.Map;
import java.util.List;

@RestController
public class ApplianceController {
    private final ApplianceService appliances; private final CollectionService collection; private final MetricService metrics; private final ReportService reports;

    /** Creates the REST controller with its appliance, collection, metric, and reporting services. */
    public ApplianceController(ApplianceService appliances, CollectionService collection, MetricService metrics, ReportService reports) { this.appliances = appliances; this.collection = collection; this.metrics = metrics; this.reports = reports; }

    /** Returns API discovery links when the service root is opened in a browser. */
    @GetMapping("/") public Map<String, Object> index() { return Map.of("service", "IoT Appliance Platform", "endpoints", Map.of("appliances", "/api/appliances", "collect", "/api/collections/run", "metrics", "/api/metrics?start={ISO-8601}&end={ISO-8601}", "reports", "/api/reports?start={ISO-8601}&end={ISO-8601}", "dailyReport", "/api/reports/daily/{YYYY-MM-DD}", "h2Console", "/h2-console")); }

    /** Returns all registered appliances. */
    @GetMapping("/api/appliances") public List<ApplianceResponse> list() { return appliances.findAll().stream().map(ApplianceResponse::from).toList(); }

    /** Registers an appliance and returns the persisted configuration. */
    @PostMapping("/api/appliances") @ResponseStatus(HttpStatus.CREATED) public ApplianceResponse create(@Valid @RequestBody ApplianceRequest request) { return ApplianceResponse.from(appliances.create(request)); }

    /** Replaces the configuration of one registered appliance. */
    @PutMapping("/api/appliances/{id}") public ApplianceResponse update(@PathVariable long id, @Valid @RequestBody ApplianceRequest request) { return ApplianceResponse.from(appliances.update(id, request)); }

    /** Removes an appliance from management. */
    @DeleteMapping("/api/appliances/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void delete(@PathVariable long id) { appliances.delete(id); }

    /** Immediately collects metrics from every enabled appliance for review or recovery. */
    @PostMapping("/api/collections/run") public CollectionResponse collect() { var result = collection.collect(true); return new CollectionResponse(result.appliancesCollected(), result.metricsStored(), result.failedCollections()); }

    /** Returns raw historical samples in the requested half-open time range. */
    @GetMapping("/api/metrics") public List<MetricResponse> metrics(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant start, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant end) { return metrics.findBetween(start, end).stream().map(MetricResponse::from).toList(); }

    /** Generates a UTC daily aggregate report for the requested calendar date. */
    @GetMapping("/api/reports/daily/{date}") public ReportResponse daily(@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) { Instant start = date.atStartOfDay(ZoneOffset.UTC).toInstant(); return reports.generate(start, start.plus(Duration.ofDays(1))); }

    /** Generates an aggregate report for an arbitrary half-open instant range. */
    @GetMapping("/api/reports") public ReportResponse report(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant start, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant end) { return reports.generate(start, end); }
}