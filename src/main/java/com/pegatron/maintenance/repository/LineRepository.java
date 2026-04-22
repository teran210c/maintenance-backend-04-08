package com.pegatron.maintenance.repository;

import com.pegatron.maintenance.model.Line;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LineRepository extends JpaRepository<Line, Long> {

}