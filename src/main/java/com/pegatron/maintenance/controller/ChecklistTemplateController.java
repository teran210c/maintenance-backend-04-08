package com.pegatron.maintenance.controller;

import com.pegatron.maintenance.model.ChecklistTemplate;
import com.pegatron.maintenance.repository.ChecklistTemplateRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/checklist-templates")
@CrossOrigin
public class ChecklistTemplateController {

    private final ChecklistTemplateRepository repo;

    public ChecklistTemplateController(ChecklistTemplateRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<ChecklistTemplate> getAll() {
        return repo.findAll();
    }

    @PostMapping
    public ChecklistTemplate create(@RequestBody ChecklistTemplate t) {
        return repo.save(t);
    }

    @PutMapping("/{id}")
    public ChecklistTemplate update(@PathVariable Long id, @RequestBody ChecklistTemplate t) {
        t.setId(id);
        return repo.save(t);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        repo.deleteById(id);
    }
}