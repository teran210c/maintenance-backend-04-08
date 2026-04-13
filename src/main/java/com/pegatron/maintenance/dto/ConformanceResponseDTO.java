package com.pegatron.maintenance.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class ConformanceResponseDTO {

    private List<ConformanceModuleDTO> modules;

    private int overallScore;

}
