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
import com.pegatron.maintenance.dto.ConformanceHistoryDetailedDTO;
import com.pegatron.maintenance.dto.ModuleScoreDTO;
import com.pegatron.maintenance.model.MaintenanceStatus;
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

    public List<ConformanceModuleDTO> getPerformance() {

        // 1. Traer solo maintenances COMPLETADOS
        List<MaintenanceTask> completedTasks =
                maintenanceRepository.findByStatus(MaintenanceStatus.COMPLETED);

        // 2. Agrupar resultados por nombre base de máquina
        Map<String, List<ChecklistResult>> grouped = new HashMap<>();

        for (MaintenanceTask task : completedTasks) {

            List<MaintenanceModule> modules =
                    moduleRepository.findByMaintenanceId(task.getId());

            for (MaintenanceModule module : modules) {

                List<ChecklistResult> results =
                        resultRepository.findByModule_Id(module.getId());

                String baseName = module.getModuleName(); // 👈 clave

                grouped
                        .computeIfAbsent(baseName, k -> new ArrayList<>())
                        .addAll(results);
            }
        }

        // 3. Calcular score por máquina
        List<ConformanceModuleDTO> performanceList = new ArrayList<>();

        for (Map.Entry<String, List<ChecklistResult>> entry : grouped.entrySet()) {

            String moduleName = entry.getKey();
            List<ChecklistResult> results = entry.getValue();

            int total = results.size();

            int completed = (int) results.stream()
                    .filter(r -> r.getResult() == ChecklistStatus.COMPLETED)
                    .count();

            int score = total == 0 ? 0 : (completed * 100) / total;

            performanceList.add(
                    new ConformanceModuleDTO(
                            null,          // no necesitas id aquí
                            moduleName,
                            completed,
                            total,
                            score
                    )
            );
        }

        // 4. Ordenar: peores primero
        performanceList.sort(Comparator.comparingInt(ConformanceModuleDTO::getScore));

        return performanceList.stream()
                .limit(3)
                .toList();
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

    public List<ConformanceHistoryDetailedDTO> getDetailedHistory(Long lineId) {

        List<MaintenanceTask> maintenances =
                maintenanceRepository.findByLine_IdAndStatus(
                        lineId, MaintenanceStatus.COMPLETED
                );

        List<ConformanceHistoryDetailedDTO> result = new ArrayList<>();

        for (MaintenanceTask mt : maintenances) {

            List<MaintenanceModule> modules =
                    moduleRepository.findByMaintenanceId(mt.getId());

            List<ModuleScoreDTO> moduleScores = new ArrayList<>();

            for (MaintenanceModule mm : modules) {

                List<ChecklistResult> results =
                        resultRepository.findByModule_Id(mm.getId());

                int total = results.size();

                int completed = (int) results.stream()
                        .filter(r -> r.getResult() == ChecklistStatus.COMPLETED)
                        .count();

                int score = total == 0 ? 0 : (completed * 100) / total;

                moduleScores.add(
                        new ModuleScoreDTO(mm.getModuleName(), score)
                );
            }

            result.add(new ConformanceHistoryDetailedDTO(
                    mt.getPerformedDate().toString(),
                    mt.getId(),
                    moduleScores
            ));
        }

        return result;
    }
}
