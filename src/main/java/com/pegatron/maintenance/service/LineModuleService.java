package com.pegatron.maintenance.service;

import com.pegatron.maintenance.model.LineModule;
import com.pegatron.maintenance.repository.LineModuleRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LineModuleService {

    private final LineModuleRepository repository;

    public LineModuleService(LineModuleRepository repository) {
        this.repository = repository;
    }

    public List<LineModule> getModulesByLine(Long lineId) {
        return repository.findByLineId(lineId);
    }

    public LineModule save(LineModule module) {
        return repository.save(module);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public LineModule findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("LineModule not found"));
    }
}