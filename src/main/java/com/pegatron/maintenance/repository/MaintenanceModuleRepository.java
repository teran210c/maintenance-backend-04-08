package com.pegatron.maintenance.repository;

import com.pegatron.maintenance.model.MaintenanceModule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MaintenanceModuleRepository extends JpaRepository<MaintenanceModule, Long> {

    List<MaintenanceModule> findByMaintenanceId(Long maintenanceId);

    List<MaintenanceModule> findByMaintenance_Line_Id(Long lineId);

    void deleteByMaintenance_Id(Long maintenanceId);

}