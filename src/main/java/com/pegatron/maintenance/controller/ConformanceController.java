package com.pegatron.maintenance.controller;

import com.pegatron.maintenance.dto.ConformanceHistoryDTO;
import com.pegatron.maintenance.dto.ConformanceResponseDTO;
import com.pegatron.maintenance.service.ConformanceService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/conformance")
public class ConformanceController {

    private final ConformanceService conformanceService;

    public ConformanceController(ConformanceService conformanceService) {
        this.conformanceService = conformanceService;
    }

    @GetMapping("/{lineId}")
    public ConformanceResponseDTO getConformance(@PathVariable Long lineId) {
        return conformanceService.getConformance(lineId);
    }

    @GetMapping("/history/{lineId}")
    public List<ConformanceHistoryDTO> getHistory(@PathVariable Long lineId) {

        return conformanceService.getHistory(lineId);

    }


}
