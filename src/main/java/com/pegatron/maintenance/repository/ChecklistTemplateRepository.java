package com.pegatron.maintenance.repository;

import com.pegatron.maintenance.model.ChecklistTemplate;
import com.pegatron.maintenance.model.MaintenanceType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChecklistTemplateRepository
        extends JpaRepository<ChecklistTemplate, Long> {

    List<ChecklistTemplate> findByModuleName(String moduleName);
    List<ChecklistTemplate> findByModuleNameAndMaintenanceType(String moduleName, MaintenanceType maintenanceType);


    Optional<ChecklistTemplate> findByModuleNameAndItemNameAndMaintenanceType(String moduleName, String itemName, MaintenanceType type);

    Optional<ChecklistTemplate> findByModuleNameIgnoreCaseAndItemNameIgnoreCaseAndMaintenanceType(String moduleName, String trim, MaintenanceType type);

    List<ChecklistTemplate> findByModuleNameAndMaintenanceTypeAndActiveTrue(String moduleName, MaintenanceType type);

    Optional<ChecklistTemplate> findByModuleNameIgnoreCaseAndItemNameIgnoreCaseAndMaintenanceTypeAndActiveTrue(String moduleName, String cleanName, MaintenanceType type);
}
