package com.pegatron.maintenance.service;

import com.pegatron.maintenance.model.LineModule;
import com.pegatron.maintenance.model.MaintenanceModule;
import com.pegatron.maintenance.repository.ChecklistResultRepository;
import com.pegatron.maintenance.repository.LineModuleRepository;
import com.pegatron.maintenance.repository.MaintenanceModuleRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MaintenanceModuleService {

    private final MaintenanceModuleRepository maintenanceModuleRepository;
    private final LineModuleRepository lineModuleRepository;
    private final ChecklistResultRepository checklistResultRepository;

    // 🔥 Constructor correcto (inyección completa)
    public MaintenanceModuleService(
            MaintenanceModuleRepository maintenanceModuleRepository,
            LineModuleRepository lineModuleRepository,
            ChecklistResultRepository checklistResultRepository
    ) {
        this.maintenanceModuleRepository = maintenanceModuleRepository;
        this.lineModuleRepository = lineModuleRepository;
        this.checklistResultRepository = checklistResultRepository;
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
}