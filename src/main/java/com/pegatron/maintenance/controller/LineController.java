package com.pegatron.maintenance.controller;

import com.pegatron.maintenance.model.Line;
import com.pegatron.maintenance.repository.LineRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/lines")
@CrossOrigin // Refuerza la configuración de CORS a nivel de controlador
public class LineController {

    private final LineRepository lineRepository;

    public LineController(LineRepository lineRepository) {
        this.lineRepository = lineRepository;
    }

    @GetMapping
    public List<Line> getAllLines() {
        return lineRepository.findAll();
    }

    @GetMapping("/{id}")
    public Line getLineById(@PathVariable Long id) {
        return lineRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Line not found"));
    }

    // AGREGA ESTE MÉTODO PARA EL POST (Error 405)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Line createLine(@RequestBody Line line) {
        return lineRepository.save(line);
    }

    // AGREGA ESTE MÉTODO PARA EL PUT
    @PutMapping("/{id}")
    public Line updateLine(@PathVariable Long id, @RequestBody Line lineDetails) {
        Line line = lineRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Line not found"));

        line.setName(lineDetails.getName());
        // Agrega aquí otros campos si tu modelo Line los tiene (ej. line.setStatus, etc)

        return lineRepository.save(line);
    }

    // AGREGA ESTE MÉTODO PARA EL DELETE
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLine(@PathVariable Long id) {
        if (!lineRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Line not found");
        }
        lineRepository.deleteById(id);
    }
}
