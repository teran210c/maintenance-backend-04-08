package com.pegatron.maintenance.repository;

import com.pegatron.maintenance.model.LineModule;
import com.pegatron.maintenance.model.MaintenanceType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LineModuleRepository
        extends JpaRepository<LineModule, Long> {

    List<LineModule> findByLineId(Long lineId, MaintenanceType type);
}
