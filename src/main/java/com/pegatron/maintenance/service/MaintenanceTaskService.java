package com.pegatron.maintenance.service;

import com.pegatron.maintenance.model.*;
import com.pegatron.maintenance.repository.ChecklistResultRepository;
import com.pegatron.maintenance.repository.LineModuleRepository;
import com.pegatron.maintenance.repository.MaintenanceTaskRepository;
import com.pegatron.maintenance.repository.MaintenanceModuleRepository;
import com.sun.tools.javac.Main;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static com.pegatron.maintenance.model.MaintenanceType.*;

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

    public MaintenanceTask getActiveTask(Long lineId, MaintenanceType type) {

        List<MaintenanceTask> tasks =
                repository.findActiveTasksByLineAndType(lineId, type);

        if (tasks.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "No active maintenance task");
        }

        return tasks.get(0); // el más reciente
    }

    @Transactional
    public MaintenanceTask acceptTask(Long lineId, MaintenanceType type) {

        MaintenanceTask task = getActiveTask(lineId, type);

        // cambiar estado
        task.setStatus(MaintenanceStatus.IN_PROGRESS);

        repository.save(task);

        // evitar duplicados
        List<MaintenanceModule> existingModules =
                moduleRepository.findByMaintenanceId(task.getId());

        if (!existingModules.isEmpty()) {
            return task;
        }

        // leer módulos configurados para la línea
        List<LineModule> lineModules =
                lineModuleRepository.findByLineId(lineId, type);

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
    public MaintenanceTask snoozeTask(Long lineId, MaintenanceType type, int hours) {

        MaintenanceTask task = getActiveTask(lineId, type);

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
    public MaintenanceTask rescheduleTask(Long lineId, MaintenanceType type, String date) {

        MaintenanceTask task = getActiveTask(lineId, type);

        LocalDate newDate = LocalDate.parse(date);

        task.setDueDate(newDate);

        task.setSnoozeUntil(null);

        return repository.save(task);
    }

    public List<MaintenanceModule> getModules(Long lineId, MaintenanceType type){

        MaintenanceTask task = getActiveTask(lineId, type);

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
        MaintenanceType type = task.getType();

        boolean exists =
                repository.existsByLineIdAndTypeAndStatusIn(
                        lineId,
                        type,
                        List.of(MaintenanceStatus.PENDING, MaintenanceStatus.IN_PROGRESS)
                );

        if (exists) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "There is already an active" + type + " maintenance for this line"
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
                event.put("type", task.getType());
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
            event.put("type", task.getType());
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
            event.put("type", task.getType());
            events.add(event);
        }

        return events;
    }

    @Transactional
    public MaintenanceTask completeTask(Long lineId, MaintenanceType type) {
        // 1. Obtener la tarea actual y completarla
        MaintenanceTask currentTask = getActiveTask(lineId, type);
        currentTask.setStatus(MaintenanceStatus.COMPLETED);
        currentTask.setPerformedDate(LocalDate.now());
        repository.save(currentTask);

        // 2. Crear la nueva tarea automáticamente
        List<MaintenanceType> types = List.of(
                MaintenanceType.SEMANAL,
                MaintenanceType.QUINCENAL,
                MaintenanceType.MENSUAL,
                MaintenanceType.TRIMESTRAL,
                MaintenanceType.CUATRIMESTRAL,
                MaintenanceType.SEMESTRAL,
                MaintenanceType.ANUAL
        );
        for (MaintenanceType t : types) {

            boolean exists = repository.existsByLineIdAndTypeAndStatusIn(
                    lineId,
                    t,
                    List.of(MaintenanceStatus.PENDING, MaintenanceStatus.IN_PROGRESS)
            );

            if (!exists) {
                MaintenanceTask newTask = new MaintenanceTask();
                newTask.setLine(currentTask.getLine());
                newTask.setType(t);
                newTask.setStatus(MaintenanceStatus.PENDING);

                LocalDate baseDate = currentTask.getDueDate();

                newTask.setDueDate(nexDate(t, baseDate));

                repository.save(newTask);
            }
        }
        return currentTask;
    }

    private LocalDate nexDate(MaintenanceType type, LocalDate baseDate) {
        return switch (type) {
            case SEMANAL -> baseDate.plusWeeks(1);
            case QUINCENAL -> baseDate.plusDays(15);
            case MENSUAL -> baseDate.plusMonths(1);
            case TRIMESTRAL -> baseDate.plusMonths(3);
            case CUATRIMESTRAL -> baseDate.plusMonths(4);
            case SEMESTRAL -> baseDate.plusMonths(6);
            case ANUAL -> baseDate.plusYears(1);

        };
    }

    public MaintenanceTask getActiveByLineIdAndType(Long lineId, MaintenanceType type){
        return repository.findByLineIdAndTypeAndStatusIn(
                lineId,
                type,
                List.of(MaintenanceStatus.PENDING, MaintenanceStatus.IN_PROGRESS)
        ).orElseThrow(() -> new RuntimeException("Maintenance not found"));
    }


}
