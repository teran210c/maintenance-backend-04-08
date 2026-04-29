package com.pegatron.maintenance.repository;

import com.pegatron.maintenance.model.ChecklistResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChecklistResultRepository extends JpaRepository<ChecklistResult, Long> {

    List<ChecklistResult> findByModule_Id(Long moduleId);

    Optional<ChecklistResult> findByModule_IdAndItemName(Long moduleId, String itemName);

    void deleteByModule_Maintenance_Id(Long maintenanceId);
}