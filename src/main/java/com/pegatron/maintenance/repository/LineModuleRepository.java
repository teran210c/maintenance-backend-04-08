package com.pegatron.maintenance.repository;

import com.pegatron.maintenance.model.LineModule;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;

public interface LineModuleRepository
        extends JpaRepository<LineModule, Long> {

    List<LineModule> findByLine_Id(Long lineId);
}
