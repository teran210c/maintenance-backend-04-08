package com.pegatron.maintenance.service;

import com.pegatron.maintenance.model.*;
import com.pegatron.maintenance.repository.LineModuleRepository;
import com.pegatron.maintenance.repository.MaintenanceModuleRepository;
import com.pegatron.maintenance.repository.MaintenanceTaskRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class LineModuleService {

    private final LineModuleRepository repository;
    private final MaintenanceTaskRepository maintenanceTaskRepository;
    private final MaintenanceModuleRepository maintenanceModuleRepository;

    public LineModuleService(
            LineModuleRepository repository,
            MaintenanceTaskRepository maintenanceTaskRepository,
            MaintenanceModuleRepository maintenanceModuleRepository
    ) {
        this.repository = repository;
        this.maintenanceTaskRepository = maintenanceTaskRepository;
        this.maintenanceModuleRepository = maintenanceModuleRepository;
    }

    // 🔥 FIX: type ya no se usa aquí
    public List<LineModule> getModulesByLine(Long lineId) {
        return repository.findByLine_Id(lineId);
    }

    // 🔥 FIX: se mantiene type porque sí se usa para buscar maintenance activo
    public LineModule save(LineModule module, MaintenanceType type) {
        LineModule saved = repository.save(module);

        Optional<MaintenanceTask> active = maintenanceTaskRepository
                .findByLineIdAndStatusAndType(
                        module.getLine().getId(),
                        MaintenanceStatus.IN_PROGRESS,
                        type
                );

        active.ifPresent(task -> {
            MaintenanceModule mm = new MaintenanceModule();
            mm.setMaintenance(task);
            mm.setModuleName(module.getModuleName());

            maintenanceModuleRepository.save(mm);
        });

        return saved;
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public LineModule findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("LineModule not found"));
    }
}