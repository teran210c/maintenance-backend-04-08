package com.pegatron.maintenance.service;

import com.pegatron.maintenance.model.ChecklistResult;
import com.pegatron.maintenance.model.ChecklistStatus;
import com.pegatron.maintenance.model.ChecklistTemplate;
import com.pegatron.maintenance.model.MaintenanceModule;
import com.pegatron.maintenance.repository.ChecklistResultRepository;
import com.pegatron.maintenance.repository.ChecklistTemplateRepository;
import com.pegatron.maintenance.repository.MaintenanceModuleRepository;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ChecklistResultService {

    private final ChecklistResultRepository resultRepository;
    private final ChecklistTemplateRepository templateRepository;
    private final MaintenanceModuleRepository moduleRepository;

    public ChecklistResultService(
            ChecklistResultRepository resultRepository,
            ChecklistTemplateRepository templateRepository,
            MaintenanceModuleRepository moduleRepository) {

        this.resultRepository = resultRepository;
        this.templateRepository = templateRepository;
        this.moduleRepository = moduleRepository;
    }


    public List<ChecklistResult> getResultsByModule(Long moduleId) {

        List<ChecklistResult> results = resultRepository.findByModule_Id(moduleId);

        if (!results.isEmpty()) {
            return results;
        }

        MaintenanceModule module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new RuntimeException("Module not found"));

        String moduleName = module.getModuleName();

        List<ChecklistTemplate> templates =
                templateRepository.findByModuleName(moduleName);

        List<ChecklistResult> newResults  = new ArrayList<>();

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

    public ChecklistResult saveResult(ChecklistResult result) {

        Optional<ChecklistResult> existing =
                resultRepository.findByModule_IdAndItemName(
                        result.getModule().getId(),
                        result.getItemName()
                );

        if (existing.isPresent()) {

            ChecklistResult r = existing.get();
            r.setResult(result.getResult());
            r.setNotes(result.getNotes());

            return resultRepository.save(r);
        }

        return resultRepository.save(result);
    }

}