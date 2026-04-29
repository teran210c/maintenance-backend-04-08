package com.pegatron.maintenance.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ConformanceModuleDTO {

    private Long moduleId;

    private String moduleName;

    private int completed;

    private int total;

    private int score;

}
