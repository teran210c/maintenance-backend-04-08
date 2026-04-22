package com.pegatron.maintenance.service;

import com.pegatron.maintenance.model.*;
import com.pegatron.maintenance.repository.ChecklistResultRepository;
import com.pegatron.maintenance.repository.LineModuleRepository;
import com.pegatron.maintenance.repository.MaintenanceTaskRepository;
import com.pegatron.maintenance.repository.MaintenanceModuleRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
    public class MaintenanceTaskService {

    private final MaintenanceTaskRepository repository;
    private final MaintenanceModuleRepository moduleRepository;
    private final LineModuleRepository lineModuleRepository;
    private final ChecklistResultRepository checklistResultRepository;

    public MaintenanceTaskService(
            MaintenanceTaskRepository repository,
            MaintenanceModuleRepository moduleRepository,
            LineModuleRepository lineModuleRepository,
            ChecklistResultRepository checklistResultRepository
    ) {
        this.repository = repository;
        this.moduleRepository = moduleRepository;
        this.lineModuleRepository = lineModuleRepository;
        this.checklistResultRepository = checklistResultRepository;
    }

    public MaintenanceTask getActiveTask(Long lineId) {

        List<MaintenanceTask> tasks =
                repository.findActiveTasks(lineId);

        if (tasks.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "No active maintenance task");
        }

        return tasks.get(0); // el más reciente
    }

    @Transactional
    public MaintenanceTask acceptTask(Long lineId) {

        MaintenanceTask task = getActiveTask(lineId);

        // cambiar estado
        task.setStatus(MaintenanceStatus.IN_PROGRESS);
        task.setPerformedDate(LocalDate.now());

        repository.save(task);

        // evitar duplicados
        List<MaintenanceModule> existingModules =
                moduleRepository.findByMaintenanceId(task.getId());

        if (!existingModules.isEmpty()) {
            return task;
        }

        // leer módulos configurados para la línea
        List<LineModule> lineModules =
                lineModuleRepository.findByLineId(lineId);

        // crear módulos del mantenimiento
        for (LineModule lm : lineModules) {

            MaintenanceModule module = new MaintenanceModule();

            module.setMaintenance(task);
            module.setModuleName(lm.getModuleName());

            moduleRepository.save(module);
        }

        return task;
    }

    @Transactional
    public MaintenanceTask snoozeTask(Long lineId, int hours) {

        MaintenanceTask task = getActiveTask(lineId);

        LocalDateTime now = LocalDateTime.now();

        LocalDateTime baseTime =
                task.getSnoozeUntil() != null && task.getSnoozeUntil().isAfter(now)
                        ? task.getSnoozeUntil()
                        : now;

        LocalDateTime newSnooze = baseTime.plusHours(hours);

        LocalDateTime dueDateTime = task.getDueDate().atStartOfDay();

        if (newSnooze.isAfter(dueDateTime)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Cannot snooze beyond due date"
            );
        }

        task.setSnoozeUntil(newSnooze);

        return repository.save(task);
    }

    @Transactional
    public MaintenanceTask rescheduleTask(Long lineId, String date) {

        MaintenanceTask task = getActiveTask(lineId);

        LocalDate newDate = LocalDate.parse(date);

        task.setDueDate(newDate);

        task.setSnoozeUntil(null);

        return repository.save(task);
    }

    public List<MaintenanceModule> getModules(Long lineId){

        MaintenanceTask task = getActiveTask(lineId);

        return moduleRepository.findByMaintenanceId(task.getId());

    }

    public List<MaintenanceTask> getUpcomingMaintenances() {
        LocalDate today = LocalDate.now();
        LocalDate limit = today.plusDays(14);

        return repository.findUpcomingMaintenances(today, limit);
    }

    @Transactional
    public MaintenanceTask create(MaintenanceTask task) {

        if (task.getLine() == null || task.getLine().getId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Line is required"
            );
        }

        Long lineId = task.getLine().getId();

        boolean exists = repository.existsActiveByLineId(lineId);

        if (exists) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "There is already an active maintenance for this line"
            );
        }

        if (task.getStatus() == null) {
            task.setStatus(MaintenanceStatus.PENDING);
        }

        task.setSnoozeUntil(null);
        task.setPerformedDate(null);

        return repository.save(task);
    }

    @Transactional
    public MaintenanceTask update(Long id, MaintenanceTask updated) {

        MaintenanceTask existing = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Maintenance not found"
                ));

        Long newLineId = updated.getLine().getId();

        // 🔥 solo valida si cambia de línea
        if (!existing.getLine().getId().equals(newLineId)) {

            boolean exists = repository.existsActiveByLineId(newLineId);

            if (exists) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Target line already has an active maintenance"
                );
            }
        }

        existing.setDueDate(updated.getDueDate());
        existing.setStatus(updated.getStatus());
        existing.setLine(updated.getLine());

        return repository.save(existing);
    }

    public MaintenanceTask save(MaintenanceTask task) {
        return repository.save(task);
    }

    public List<MaintenanceTask> getAll() {
        return repository.findAll();
    }

    @Transactional
    public void delete(Long id) {

        // 1. borrar checklist_results
        checklistResultRepository.deleteByModule_Maintenance_Id(id);

        // 2. borrar maintenance_modules
        moduleRepository.deleteByMaintenance_Id(id);

        // 3. borrar maintenance_task
        repository.deleteById(id);
    }

    public List<Map<String, Object>> getCalendarEvents(Long lineId) {
        List<Map<String, Object>> events = new ArrayList<>();

        // 1. Procesar COMPLETADOS
        List<MaintenanceTask> completedTasks = repository.findCompletedTasks(lineId);
        for (MaintenanceTask task : completedTasks) {
            if (task.getPerformedDate() != null) {
                Map<String, Object> event = new HashMap<>();
                event.put("date", task.getPerformedDate());
                event.put("status", "COMPLETED");
                events.add(event);
            }
        }

        // 2. NUEVO: EN PROGRESO
        // Necesitas crear este método en tu repository (ej. findInProgressTasks)
        List<MaintenanceTask> inProgressTasks = repository.findInProgressTasks(lineId);
        for (MaintenanceTask task : inProgressTasks) {
            Map<String, Object> event = new HashMap<>();
            // Usamos la fecha de vencimiento o la fecha en que se inició
            event.put("date", task.getDueDate());
            event.put("status", "IN_PROGRESS");
            events.add(event);
        }

        List<MaintenanceTask> pendingTasks = repository.findPendingTasks(lineId);
        for (MaintenanceTask task : pendingTasks) {
            Map<String, Object> event = new HashMap<>();
            LocalDate displayDate = (task.getSnoozeUntil() != null)
                    ? task.getSnoozeUntil().toLocalDate()
                    : task.getDueDate();

            event.put("date", displayDate);
            event.put("status", "PENDING");
            events.add(event);
        }

        return events;
    }

    @Transactional
    public MaintenanceTask completeTask(Long lineId) {
        // 1. Obtener la tarea actual y completarla
        MaintenanceTask currentTask = getActiveTask(lineId);
        currentTask.setStatus(MaintenanceStatus.COMPLETED);
        currentTask.setPerformedDate(LocalDate.now());
        repository.save(currentTask);

        // 2. Crear la nueva tarea automáticamente
        MaintenanceTask nextTask = new MaintenanceTask();
        nextTask.setLine(currentTask.getLine());
        nextTask.setType(currentTask.getType()); // Mantiene si es Quincenal o Mensual
        nextTask.setStatus(MaintenanceStatus.PENDING); // O el estado inicial que uses

        // 3. Calcular fecha según el tipo (Enum que creamos antes)
        if (currentTask.getType() == MaintenanceType.QUINCENAL) {
            nextTask.setDueDate(currentTask.getDueDate().plusDays(15));
        } else {
            nextTask.setDueDate(currentTask.getDueDate().plusMonths(1));
        }

        return repository.save(nextTask);
    }
    }

}
