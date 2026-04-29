package com.pegatron.maintenance.controller;

import com.pegatron.maintenance.model.MaintenanceModule;
import com.pegatron.maintenance.service.MaintenanceModuleService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/maintenance-modules")
public class MaintenanceModuleController {

    private final MaintenanceModuleService service;

    public MaintenanceModuleController(MaintenanceModuleService service) {
        this.service = service;
    }

    @GetMapping("/maintenance/{maintenanceId}")
    public List<MaintenanceModule> getByMaintenance(@PathVariable Long maintenanceId) {
        return service.getByMaintenance(maintenanceId);
    }
}