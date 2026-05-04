package com.pegatron.maintenance.dto;

public class ModuleConformanceDTO {

    private String moduleName;
    private int completed;
    private int total;
    private int score;

    public ModuleConformanceDTO(String moduleName, int completed, int total, int score) {
        this.moduleName = moduleName;
        this.completed = completed;
        this.total = total;
        this.score = score;
    }

    public String getModuleName() { return moduleName; }
    public int getCompleted() { return completed; }
    public int getTotal() { return total; }
    public int getScore() { return score; }
}