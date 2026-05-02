package com.pegatron.maintenance.dto;

import java.util.List;

public class ConformanceHistoryDetailedDTO {

    private String date;
    private Long maintenanceId;
    private List<ModuleScoreDTO> modules;

    public ConformanceHistoryDetailedDTO(String date, Long maintenanceId, List<ModuleScoreDTO> modules) {
        this.date = date;
        this.maintenanceId = maintenanceId;
        this.modules = modules;
    }

    public String getDate() {
        return date;
    }

    public Long getMaintenanceId() {
        return maintenanceId;
    }

    public List<ModuleScoreDTO> getModules() {
        return modules;
    }
}