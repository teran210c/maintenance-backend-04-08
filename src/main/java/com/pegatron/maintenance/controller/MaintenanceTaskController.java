package com.pegatron.maintenance.controller;

import com.pegatron.maintenance.model.MaintenanceModule;
import com.pegatron.maintenance.model.MaintenanceTask;
import com.pegatron.maintenance.model.MaintenanceType;
import com.pegatron.maintenance.service.MaintenanceTaskService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/maintenance")
public class MaintenanceTaskController {

    private final MaintenanceTaskService service;

    public MaintenanceTaskController(MaintenanceTaskService service) {
        this.service = service;
    }

    @GetMapping("/active/{lineId}")
    public MaintenanceTask getActiveByLineAndType(
            @PathVariable Long lineId,
            @RequestParam MaintenanceType type
    ) {
        return service.getActiveByLineIdAndType(lineId, type);
    }

    @PostMapping("/accept/{lineId}")
    public MaintenanceTask acceptTask(@PathVariable Long lineId, @RequestParam MaintenanceType type) {

        return service.acceptTask(lineId, type);
    }

    @PostMapping("/snooze/{lineId}")
    public MaintenanceTask snoozeTask(
            @PathVariable Long lineId,
            @RequestParam MaintenanceType type,
            @RequestParam int hours) {

        return service.snoozeTask(lineId, type, hours);
    }

    @PostMapping("/reschedule/{lineId}")
    public MaintenanceTask rescheduleTask(
            @PathVariable Long lineId,
            @RequestParam MaintenanceType type,
            @RequestParam String date) {

        return service.rescheduleTask(lineId, type, date);
    }

    @GetMapping("/history/{lineId}")
    public List<Map<String, Object>> getCalendarEvents(@PathVariable Long lineId) {
        return service.getCalendarEvents(lineId);
    }

    @GetMapping("/modules/{lineId}")
    public List<MaintenanceModule> getModules(@PathVariable Long lineId, @RequestParam MaintenanceType type){

        return service.getModules(lineId, type);
    }

    @GetMapping("/upcoming")
    public List<MaintenanceTask> getUpcomingMaintenances() {
        return service.getUpcomingMaintenances();
    }

    @GetMapping
    public List<MaintenanceTask> getAll() {
        return service.getAll();
    }

    @PostMapping
    public MaintenanceTask create(@RequestBody MaintenanceTask task) {
        return service.create(task);
    }

    @PutMapping("/{id}")
    public MaintenanceTask update(@PathVariable Long id, @RequestBody MaintenanceTask task) {
        return service.update(id, task);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @PostMapping("/complete/{lineId}")
    public MaintenanceTask completeTask(
            @PathVariable Long lineId,
            @RequestParam MaintenanceType type
    ) {
        return service.completeTask(lineId, type);
    }


}
