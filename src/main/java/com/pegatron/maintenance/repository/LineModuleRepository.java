package com.pegatron.maintenance.repository;

import com.pegatron.maintenance.model.LineModule;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;
import java.util.Optional;

public interface LineModuleRepository
        extends JpaRepository<LineModule, Long> {

    List<LineModule> findByLine_Id(Long lineId);

    List<LineModule> findByLine_IdAndActiveTrue(Long lineId);

    Optional<LineModule> findByLine_IdAndModuleName(Long lineId, String moduleName);
}
