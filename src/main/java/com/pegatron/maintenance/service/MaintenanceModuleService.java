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

    @Transactional
    public void delete(Long id) {

        MaintenanceModule maintenanceModule = maintenanceModuleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("MaintenanceModule not found"));

        // 🔥 1. obtener SU lineModule
        LineModule lineModule = maintenanceModule.getLineModule();

        // 🔥 2. borrar checklist
        checklistResultRepository.deleteByModule_Id(id);

        // 🔥 3. borrar maintenance module
        maintenanceModuleRepository.delete(maintenanceModule);

        // 🔥 4. verificar si alguien más usa ese lineModule
        long usage = maintenanceModuleRepository.countByLineModule_Id(lineModule.getId());

        // 🔥 5. si ya nadie lo usa → desactivar
        if (usage == 0) {
            lineModule.setActive(false);
            lineModuleRepository.save(lineModule);
        }
    }

    @Transactional
    public void addModule(Long lineId, Long maintenanceId, String moduleName) {

        String cleanName = moduleName.trim().toUpperCase();

        Line line = lineRepository.findById(lineId)
                .orElseThrow(() -> new RuntimeException("Line not found"));

        MaintenanceTask maintenance = maintenanceTaskRepository.findById(maintenanceId)
                .orElseThrow(() -> new RuntimeException("Maintenance not found"));

        // 🔥 1. Obtener o crear template (LineModule)
        LineModule lineModule = lineModuleRepository
                .findByLine_IdAndModuleName(lineId, cleanName)
                .orElse(null);

        if (lineModule == null) {
            lineModule = new LineModule();
            lineModule.setLine(line);
            lineModule.setModuleName(cleanName);
            lineModule.setActive(true);
            lineModule = lineModuleRepository.save(lineModule);
        } else {
            lineModule.setActive(true);
            lineModuleRepository.save(lineModule);
        }

        // 🔥 2. Crear SIEMPRE nueva instancia (MaintenanceModule)
        MaintenanceModule mm = new MaintenanceModule();
        mm.setMaintenance(maintenance);
        mm.setLineModule(lineModule); // 🔥 ESTA ES LA CLAVE
        mm.setModuleName(cleanName);  // opcional (para UI)

        maintenanceModuleRepository.save(mm);
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