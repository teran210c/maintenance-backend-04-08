package com.pegatron.maintenance.controller;

import com.pegatron.maintenance.model.ChecklistResult;
import com.pegatron.maintenance.model.ChecklistTemplate;
import com.pegatron.maintenance.model.MaintenanceTask;
import com.pegatron.maintenance.model.MaintenanceType;
import com.pegatron.maintenance.repository.ChecklistResultRepository;
import com.pegatron.maintenance.repository.ChecklistTemplateRepository;
import com.pegatron.maintenance.service.ChecklistResultService;
import jakarta.transaction.Transactional;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/checklist")
public class ChecklistResultController {

    private final ChecklistResultService service;
    private final SimpMessagingTemplate messagingTemplate;
    private final ChecklistResultRepository checklistResultRepository;
    private  final ChecklistTemplateRepository checklistTemplateRepository;

    public ChecklistResultController(
            ChecklistResultService service,
            SimpMessagingTemplate messagingTemplate,
            ChecklistResultRepository checklistResultRepository, ChecklistTemplateRepository checklistTemplateRepository
    ) {
        this.service = service;
        this.messagingTemplate = messagingTemplate;
        this.checklistResultRepository = checklistResultRepository;
        this.checklistTemplateRepository = checklistTemplateRepository;
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

    @PostMapping("/add-task")
    public ChecklistResult addTask(@RequestBody Map<String, String> body) {
        Long moduleId = Long.parseLong(body.get("moduleId"));
        String itemName = body.get("itemName");

        return service.addTaskToModule(moduleId, itemName);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @PutMapping("/{id}")
    public ChecklistResult updateTask(
            @PathVariable Long id,
            @RequestBody Map<String, String> body
    ) {
     String newName = body.get("itemName");

     return service.updateTask(id, newName);
    }

}
