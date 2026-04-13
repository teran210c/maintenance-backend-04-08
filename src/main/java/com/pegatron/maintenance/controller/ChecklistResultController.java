package com.pegatron.maintenance.controller;

import com.pegatron.maintenance.model.ChecklistResult;
import com.pegatron.maintenance.service.ChecklistResultService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/checklist")
public class ChecklistResultController {

    private final ChecklistResultService service;

    public ChecklistResultController(ChecklistResultService service) {
        this.service = service;
    }

    @GetMapping("/{moduleId}")
    public List<ChecklistResult> getResults(@PathVariable Long moduleId) {
        return service.getResultsByModule(moduleId);
    }

    @PostMapping
    public ChecklistResult saveResult(@RequestBody ChecklistResult result) {
        return service.saveResult(result);
    }
}