package com.pegatron.maintenance.service;

import com.pegatron.maintenance.model.*;
import com.pegatron.maintenance.repository.ChecklistResultRepository;
import com.pegatron.maintenance.repository.ChecklistTemplateRepository;
import com.pegatron.maintenance.repository.MaintenanceModuleRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ChecklistResultService {

    private final ChecklistResultRepository resultRepository;
    private final ChecklistTemplateRepository templateRepository;
    private final MaintenanceModuleRepository moduleRepository;

    public ChecklistResultService(ChecklistResultRepository resultRepository, ChecklistTemplateRepository templateRepository, MaintenanceModuleRepository moduleRepository) {

        this.resultRepository = resultRepository;
        this.templateRepository = templateRepository;
        this.moduleRepository = moduleRepository;
    }


    public List<ChecklistResult> getResultsByModule(Long moduleId) {

        return resultRepository.findByModule_Id(moduleId);
    }

    @Transactional
    public ChecklistResult saveResult(ChecklistResult result) {
        if (result.getId() == null) {
            return resultRepository.save(result);
        }

        // Buscamos el registro real
        return resultRepository.findById(result.getId()).map(existing -> {
            // ACTUALIZAMOS SOLO LOS CAMPOS DE ESTADO Y NOTAS
            existing.setResult(result.getResult());
            existing.setNotes(result.getNotes());

            // NO HACEMOS MERGE DEL OBJETO 'result' QUE VIENE DEL FRONT
            // simplemente guardamos el que ya está gestionado por Hibernate (existing)
            return resultRepository.save(existing);
        }).orElseThrow(() -> new RuntimeException("No se encontró el item con ID: " + result.getId()));
    }


    @Transactional
    public ChecklistResult addTaskToModule(Long moduleId, String itemName) {

        String cleanName = itemName.trim();

        MaintenanceModule module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new RuntimeException("Module not found"));

        MaintenanceTask task = module.getMaintenance();

        // 🔥 buscar o crear template
        Optional<ChecklistTemplate> existingTemplate = templateRepository
                .findByModuleNameIgnoreCaseAndItemNameIgnoreCaseAndMaintenanceType(
                        module.getModuleName(),
                        cleanName,
                        task.getType()
                );

        ChecklistTemplate template;

        if (existingTemplate.isPresent()) {
            template = existingTemplate.get();
        } else {
            template = new ChecklistTemplate();
            template.setItemName(cleanName);
            template.setModuleName(module.getModuleName());
            template.setMaintenanceType(task.getType());

            templateRepository.save(template);
        }

        // 🔥 crear result ligado al template (por ID)
        ChecklistResult newItem = new ChecklistResult();
        newItem.setModule(module);
        newItem.setTemplate(template);
        newItem.setItemName(template.getItemName());
        newItem.setMaintenanceType(task.getType());
        newItem.setResult(ChecklistStatus.PENDING);
        newItem.setNotes("");

        return resultRepository.save(newItem);
    }

    public void delete(Long id) {
        resultRepository.deleteById(id);
    }

    @Transactional
    public ChecklistResult updateTask(Long id, String newName) {

        String cleanName = newName.trim();

        ChecklistResult result = resultRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        System.out.println("Template: " + result.getTemplate());
        if (result.getTemplate() != null) {
            System.out.println("Template ID: " + result.getTemplate().getId());
            System.out.println("Template Name: " + result.getTemplate().getItemName());
        }
        // 🔥 actualizar result
        result.setItemName(cleanName);

        // 🔥 actualizar template (si existe)
        if (result.getTemplate() != null) {
            ChecklistTemplate template = result.getTemplate();
            template.setItemName(cleanName);

            // opcional: mantener consistencia
            result.setMaintenanceType(template.getMaintenanceType());
        }

        return resultRepository.save(result);
    }
}
