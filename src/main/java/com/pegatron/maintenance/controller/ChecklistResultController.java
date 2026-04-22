package com.pegatron.maintenance.controller;

import com.pegatron.maintenance.model.ChecklistResult;
import com.pegatron.maintenance.service.ChecklistResultService;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/checklist")
public class ChecklistResultController {

    private final ChecklistResultService service;
    private final SimpMessagingTemplate messagingTemplate;

    public ChecklistResultController(
            ChecklistResultService service,
            SimpMessagingTemplate messagingTemplate
    ) {
        this.service = service;
        this.messagingTemplate = messagingTemplate;
    }

    @GetMapping("/{moduleId}")
    public List<ChecklistResult> getResults(@PathVariable Long moduleId) {
        return service.getResultsByModule(moduleId);
    }

    @PostMapping
    public ChecklistResult saveResult(@RequestBody ChecklistResult result) {
        ChecklistResult saved = service.saveResult(result);

        // 🔥 AQUÍ ESTÁ LA MAGIA
        messagingTemplate.convertAndSend("/topic/updates", saved);

        return saved;
    }
}