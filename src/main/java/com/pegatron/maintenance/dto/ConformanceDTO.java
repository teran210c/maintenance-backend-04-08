package com.pegatron.maintenance.dto;
import com.pegatron.maintenance.dto.ModuleConformanceDTO;

import java.util.List;

public class ConformanceDTO {

    private List<ModuleConformanceDTO> modules;
    private int overallScore;

    public ConformanceDTO(List<ModuleConformanceDTO> modules, int overallScore) {
        this.modules = modules;
        this.overallScore = overallScore;
    }

    public List<ModuleConformanceDTO> getModules() {
        return modules;
    }

    public int getOverallScore() {
        return overallScore;
    }
}