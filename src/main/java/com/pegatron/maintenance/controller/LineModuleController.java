package com.pegatron.maintenance.controller;

import com.pegatron.maintenance.model.LineModule;
import com.pegatron.maintenance.model.MaintenanceType;
import com.pegatron.maintenance.service.LineModuleService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/line-modules")
public class LineModuleController {

    private final LineModuleService service;

    public LineModuleController(LineModuleService service) {
        this.service = service;
    }

    @GetMapping("/line/{lineId}")
    public List<LineModule> getModulesByLine(
            @PathVariable Long lineId,
            @RequestParam(required = false) MaintenanceType type
    ) {
        // Por ahora solo logueamos, pero ya recibimos el dato
        return service.getModulesByLine(lineId, type);
    }

    @PostMapping
    public LineModule create(
            @RequestBody LineModule module,
            @RequestParam MaintenanceType type // Recibimos el tipo desde la URL
    ) {
        return service.save(module, type);
    }

    @PutMapping("/{id}")
    public LineModule update(
            @PathVariable Long id,
            @RequestBody LineModule updated,
            @RequestParam MaintenanceType type
    ) {

        LineModule existing = service.findById(id);

        existing.setModuleName(updated.getModuleName());
        existing.setLine(updated.getLine());

        return service.save(existing, type);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
