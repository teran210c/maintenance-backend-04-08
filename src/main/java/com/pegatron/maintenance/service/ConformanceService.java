package com.pegatron.maintenance.service;

import com.pegatron.maintenance.dto.*;
import com.pegatron.maintenance.model.ChecklistResult;
import com.pegatron.maintenance.model.ChecklistStatus;
import com.pegatron.maintenance.model.MaintenanceModule;
import com.pegatron.maintenance.model.MaintenanceTask;
import com.pegatron.maintenance.repository.ChecklistResultRepository;
import com.pegatron.maintenance.repository.MaintenanceModuleRepository;
import com.pegatron.maintenance.repository.MaintenanceTaskRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ConformanceService {

    private final ChecklistResultRepository resultRepository;
    private final MaintenanceModuleRepository moduleRepository;
    private final MaintenanceTaskRepository maintenanceRepository;

    public ConformanceService(
            ChecklistResultRepository resultRepository,
            MaintenanceModuleRepository moduleRepository,
            MaintenanceTaskRepository maintenanceRepository
    ) {
        this.resultRepository = resultRepository;
        this.moduleRepository = moduleRepository;
        this.maintenanceRepository = maintenanceRepository;
    }

    public ConformanceResponseDTO getConformance(Long maintenanceId) {

        MaintenanceTask task = maintenanceRepository.findById(maintenanceId)
                .orElseThrow(() -> new RuntimeException("Maintenance not found"));

        List<MaintenanceModule> modules =
                moduleRepository.findByMaintenanceId(maintenanceId);

        List<ConformanceModuleDTO> moduleDTOs = new ArrayList<>();

        // Contar cuántas máquinas hay de cada tipo
        Map<String, Long> moduleTypeCount = new HashMap<>();

        for (MaintenanceModule module : modules) {
            moduleTypeCount.put(
                    module.getModuleName(),
                    moduleTypeCount.getOrDefault(module.getModuleName(), 0L) + 1
            );
        }

        // Contador para numerarlas
        Map<String, Integer> moduleNumbering = new HashMap<>();

        for (MaintenanceModule module : modules) {

            List<ChecklistResult> results =
                    resultRepository.findByModule_Id(module.getId());

            System.out.println("Module: " + module.getModuleName());
            System.out.println("Results: " + results.size());

            int total = results.size();

            int completed = (int) results.stream()
                    .filter(r -> r.getResult() == ChecklistStatus.COMPLETED)
                    .count();

            results.forEach(r -> System.out.println("Result: " + r.getResult()));

            int score = total == 0 ? 0 : (completed * 100) / total;

            String baseName = module.getModuleName();
            String displayName = baseName;

            // Si hay más de una máquina del mismo tipo → numerar
            if (moduleTypeCount.get(baseName) > 1) {

                int number = moduleNumbering.getOrDefault(baseName, 0) + 1;
                moduleNumbering.put(baseName, number);

                displayName = baseName + " " + number;
            }

            moduleDTOs.add(
                    new ConformanceModuleDTO(
                            module.getId(),
                            displayName,
                            completed,
                            total,
                            score
                    )
            );
        }



        int overallScore = moduleDTOs.isEmpty()
                ? 0
                : moduleDTOs.stream()
                .mapToInt(ConformanceModuleDTO::getScore)
                .sum() / moduleDTOs.size();

        return new ConformanceResponseDTO(moduleDTOs, overallScore);
    }

    public List<ConformanceHistoryDTO> getHistory(Long lineId) {

        List<MaintenanceModule> modules =
                moduleRepository.findByMaintenance_Line_Id(lineId);

        Map<String, List<ChecklistResult>> byDate = new HashMap<>();

        for (MaintenanceModule module : modules) {

            List<ChecklistResult> results =
                    resultRepository.findByModule_Id(module.getId());

            for (ChecklistResult r : results) {

                String date = r.getModule()
                        .getMaintenance()
                        .getDueDate()
                        .toString();

                byDate
                        .computeIfAbsent(date, k -> new ArrayList<>())
                        .add(r);
            }
        }

        List<ConformanceHistoryDTO> history = new ArrayList<>();

        for (String date : byDate.keySet()) {

            List<ChecklistResult> results = byDate.get(date);

            int total = results.size();

            int completed = (int) results.stream()
                    .filter(r -> r.getResult() == ChecklistStatus.COMPLETED)
                    .count();

            int score = total == 0 ? 0 : (completed * 100) / total;

            history.add(new ConformanceHistoryDTO(date, score));
        }

        history.sort(Comparator.comparing(ConformanceHistoryDTO::getDate));

        return history;
    }

}
