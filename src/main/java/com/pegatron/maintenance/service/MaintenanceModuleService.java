package com.pegatron.maintenance.service;

import com.pegatron.maintenance.model.Line;
import com.pegatron.maintenance.model.LineModule;
import com.pegatron.maintenance.model.MaintenanceModule;
import com.pegatron.maintenance.model.MaintenanceTask;
import com.pegatron.maintenance.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MaintenanceModuleService {

    private final MaintenanceModuleRepository maintenanceModuleRepository;
    private final LineModuleRepository lineModuleRepository;
    private final ChecklistResultRepository checklistResultRepository;
    private final LineRepository lineRepository;
    private final MaintenanceTaskRepository maintenanceTaskRepository;

    // 🔥 Constructor correcto (inyección completa)
    public MaintenanceModuleService(
            MaintenanceModuleRepository maintenanceModuleRepository,
            LineModuleRepository lineModuleRepository,
            ChecklistResultRepository checklistResultRepository,
            LineRepository lineRepository,
            MaintenanceTaskRepository maintenanceTaskRepository
    ) {
        this.maintenanceModuleRepository = maintenanceModuleRepository;
        this.lineModuleRepository = lineModuleRepository;
        this.checklistResultRepository = checklistResultRepository;
        this.lineRepository = lineRepository;
        this.maintenanceTaskRepository = maintenanceTaskRepository;
    }

    // 🔥 Obtener módulos por maintenance
    public List<MaintenanceModule> getByMaintenance(Long maintenanceId) {
        return maintenanceModuleRepository.findByMaintenanceId(maintenanceId);
    }

    // 🔥 Delete completo (presente + futuro)
    @Transactional
    public void delete(Long id) {

        // 🔥 1. obtener MaintenanceModule real
        MaintenanceModule maintenanceModule = maintenanceModuleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("MaintenanceModule not found"));

        String moduleName = maintenanceModule.getModuleName();
        Long lineId = maintenanceModule.getMaintenance().getLine().getId();

        // 🔥 2. borrar checklist results del módulo actual
        checklistResultRepository.deleteByModule_Id(id);

        // 🔥 3. borrar maintenance module actual
        maintenanceModuleRepository.delete(maintenanceModule);

        // 🔥 4. buscar LineModule correspondiente
        LineModule lineModule = lineModuleRepository
                .findByLine_IdAndModuleName(lineId, moduleName)
                .orElseThrow(() -> new RuntimeException("LineModule not found"));

        // 🔥 5. desactivar para futuros
        lineModule.setActive(false);
        lineModuleRepository.save(lineModule);
    }

    @Transactional
    public void addModule(Long lineId, Long maintenanceId, String moduleName) {

        String cleanName = moduleName.trim().toUpperCase();

        // ✅ ahora sí correcto
        Line line = lineRepository.findById(lineId)
                .orElseThrow(() -> new RuntimeException("Line not found"));

        MaintenanceTask maintenance = maintenanceTaskRepository.findById(maintenanceId)
                .orElseThrow(() -> new RuntimeException("Maintenance not found"));

        // 🔥 LineModule (futuro)
        LineModule lineModule = lineModuleRepository
                .findByLine_IdAndModuleName(lineId, cleanName)
                .orElse(null);

        if (lineModule == null) {
            lineModule = new LineModule();
            lineModule.setLine(line);
            lineModule.setModuleName(cleanName);
            lineModule.setActive(true);
            lineModuleRepository.save(lineModule);
        } else {
            lineModule.setActive(true);
        }

        // 🔥 MaintenanceModule (presente)
        boolean exists = maintenanceModuleRepository
                .existsByMaintenance_IdAndModuleName(maintenanceId, cleanName);

        if (!exists) {
            MaintenanceModule mm = new MaintenanceModule();
            mm.setMaintenance(maintenance);
            mm.setModuleName(cleanName);
            maintenanceModuleRepository.save(mm);
        }
    }

    @Transactional
    public void update(Long id, String newName) {

        String cleanName = newName.trim().toUpperCase();

        // 🔥 1. obtener MaintenanceModule actual
        MaintenanceModule mm = maintenanceModuleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("MaintenanceModule not found"));

        String oldName = mm.getModuleName();
        Long lineId = mm.getMaintenance().getLine().getId();

        // 🔥 2. actualizar TODOS los MaintenanceModules de ese nombre
        List<MaintenanceModule> modules =
                maintenanceModuleRepository.findByMaintenance_Line_IdAndModuleName(
                        lineId,
                        oldName
                );

        for (MaintenanceModule m : modules) {
            m.setModuleName(cleanName);
        }

        maintenanceModuleRepository.saveAll(modules);

        // 🔥 3. actualizar LineModule (futuro)
        LineModule lineModule = lineModuleRepository
                .findByLine_IdAndModuleName(lineId, oldName)
                .orElseThrow(() -> new RuntimeException("LineModule not found"));

        lineModule.setModuleName(cleanName);
        lineModuleRepository.save(lineModule);
    }
}