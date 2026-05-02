package com.pegatron.maintenance.controller;

import com.pegatron.maintenance.model.MaintenanceModule;
import com.pegatron.maintenance.service.MaintenanceModuleService;
import org.springframework.web.bind.annotation.*;

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

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}