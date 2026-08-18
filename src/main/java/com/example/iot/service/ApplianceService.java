package com.example.iot.service;

import com.example.iot.api.ApiModels.ApplianceRequest;
import com.example.iot.domain.Appliance;
import com.example.iot.repository.ApplianceRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class ApplianceService {
    private final ApplianceRepository repository;

    /** Creates the service with its appliance persistence dependency. */
    public ApplianceService(ApplianceRepository repository) { this.repository = repository; }

    /** Retrieves every registered appliance. */
    public List<Appliance> findAll() { return repository.findAll(); }

    /** Retrieves one appliance or fails when the supplied identifier is unknown. */
    public Appliance find(long id) { return repository.findById(id).orElseThrow(() -> new NoSuchElementException("Appliance not found: " + id)); }

    /** Persists a new appliance from a validated API request. */
    public Appliance create(ApplianceRequest request) { return repository.save(new Appliance(request.name(), request.type(), request.vendor(), request.collectionIntervalSeconds())); }

    /** Updates the supplied appliance and treats an omitted enabled flag as enabled. */
    public Appliance update(long id, ApplianceRequest request) { Appliance appliance = find(id); appliance.update(request.name(), request.type(), request.vendor(), request.collectionIntervalSeconds(), request.enabled() == null || request.enabled()); return repository.save(appliance); }

    /** Deletes an appliance after verifying that it exists. */
    public void delete(long id) { repository.delete(find(id)); }
}