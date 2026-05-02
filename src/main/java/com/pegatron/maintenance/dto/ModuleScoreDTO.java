package com.pegatron.maintenance.dto;

public class ModuleScoreDTO {

    private String moduleName;
    private int score;

    public ModuleScoreDTO(String moduleName, int score) {
        this.moduleName = moduleName;
        this.score = score;
    }

    public String getModuleName() {
        return moduleName;
    }

    public int getScore() {
        return score;
    }
}