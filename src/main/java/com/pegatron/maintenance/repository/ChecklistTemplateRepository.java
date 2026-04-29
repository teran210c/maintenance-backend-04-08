package com.pegatron.maintenance.repository;

import com.pegatron.maintenance.model.ChecklistTemplate;
import com.pegatron.maintenance.model.MaintenanceType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChecklistTemplateRepository
        extends JpaRepository<ChecklistTemplate, Long> {

    List<ChecklistTemplate> findByModuleName(String moduleName);
    List<ChecklistTemplate> findByModuleNameAndMaintenanceType(String moduleName, MaintenanceType maintenanceType);

}
