package com.pegatron.maintenance.service;

import com.pegatron.maintenance.model.ChecklistResult;
import com.pegatron.maintenance.model.ChecklistStatus;
import com.pegatron.maintenance.model.ChecklistTemplate;
import com.pegatron.maintenance.model.MaintenanceModule;
import com.pegatron.maintenance.repository.ChecklistResultRepository;
import com.pegatron.maintenance.repository.ChecklistTemplateRepository;
import com.pegatron.maintenance.repository.MaintenanceModuleRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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

        List<ChecklistResult> results = resultRepository.findByModule_Id(moduleId);

        if (!results.isEmpty()) {
            return results;
        }

        MaintenanceModule module = moduleRepository.findById(moduleId).orElseThrow(() -> new RuntimeException("Module not found"));

        String moduleName = module.getModuleName();

        List<ChecklistTemplate> templates = templateRepository.findByModuleName(moduleName);

        List<ChecklistResult> newResults = new ArrayList<>();

        for (ChecklistTemplate template : templates) {

            ChecklistResult result = new ChecklistResult();

            result.setModule(module);
            result.setItemName(template.getItemName());
            result.setResult(ChecklistStatus.PENDING);
            result.setNotes("");

            newResults.add(resultRepository.save(result));
        }

        return newResults;
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
        MaintenanceModule module = moduleRepository.findById(moduleId).orElseThrow(() -> new RuntimeException("Module not found"));

        ChecklistResult newItem = new ChecklistResult();
        newItem.setModule(module);
        newItem.setItemName(itemName);

        newItem.setResult(ChecklistStatus.PENDING);

        newItem.setNotes("");

        return resultRepository.save(newItem);

    }

    public void delete(Long id) {
        resultRepository.deleteById(id);
    }

}
