package com.pegatron.maintenance.controller;

import com.pegatron.maintenance.dto.*;
import com.pegatron.maintenance.service.ConformanceService;
import org.springframework.web.bind.annotation.*;
import com.pegatron.maintenance.dto.ConformanceDTO;

import java.util.List;

@RestController
@RequestMapping("/api/conformance")
public class ConformanceController {

    private final ConformanceService conformanceService;

    public ConformanceController(ConformanceService conformanceService) {
        this.conformanceService = conformanceService;
    }

//    @GetMapping("/{maintenanceId}")
//    public ConformanceResponseDTO getConformance(@PathVariable Long maintenanceId) {
//        return conformanceService.getConformance(maintenanceId);
//    }

    @GetMapping("/{maintenanceId}")
    public ConformanceDTO getConformance(@PathVariable Long maintenanceId) {
        return conformanceService.getConformance(maintenanceId);
    }

    @GetMapping("/performance")
    public List<ConformanceModuleDTO> getPerformance() {
        return conformanceService.getPerformance();
    }

    @GetMapping("/history/{lineId}")
    public List<ConformanceHistoryDTO> getHistory(@PathVariable Long lineId) {

        return conformanceService.getHistory(lineId);

    }

    @GetMapping("/history-detailed/{lineId}")
    public List<ConformanceHistoryDetailedDTO> getDetailedHistory(@PathVariable Long lineId) {
        return conformanceService.getDetailedHistory(lineId);
    }


}
