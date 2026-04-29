package com.pegatron.maintenance.service;

import com.pegatron.maintenance.model.MaintenanceModule;
import com.pegatron.maintenance.repository.MaintenanceModuleRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MaintenanceModuleService {

    private final MaintenanceModuleRepository repository;

    public MaintenanceModuleService(MaintenanceModuleRepository repository) {
        this.repository = repository;
    }

    public List<MaintenanceModule> getByMaintenance(Long maintenanceId) {
        return repository.findByMaintenanceId(maintenanceId);
    }
}