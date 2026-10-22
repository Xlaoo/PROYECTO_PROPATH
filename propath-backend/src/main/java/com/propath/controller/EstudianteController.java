package com.propath.controller;

import com.propath.entity.Estudiante;
import com.propath.service.EstudianteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/estudiantes")
public class EstudianteController {

    private final EstudianteService estudianteService;

    public EstudianteController(EstudianteService estudianteService) {
        this.estudianteService = estudianteService;
    }

    @GetMapping
    public ResponseEntity<List<Estudiante>> listar() {
        return ResponseEntity.ok(estudianteService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Estudiante> buscarPorId(@PathVariable("id") Integer id) {
        return ResponseEntity.ok(estudianteService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<Estudiante> registrar(@RequestBody Estudiante estudiante) {
        Estudiante creado = estudianteService.guardar(estudiante);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Estudiante> actualizar(@PathVariable("id") Integer id, @RequestBody Estudiante estudiante) {
        estudiante.setId(id);
        Estudiante actualizado = estudianteService.guardar(estudiante);
        return ResponseEntity.ok(actualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable("id") Integer id) {
        estudianteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<Estudiante> cambiarEstado(@PathVariable("id") Integer id, @RequestBody java.util.Map<String, String> body) {
        String estadoStr = body != null ? body.get("estado") : null;
        if (estadoStr == null || estadoStr.trim().isEmpty()) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.BAD_REQUEST, "El estado es obligatorio.");
        }
        com.propath.entity.Usuario.Estado estado = com.propath.entity.Usuario.Estado.valueOf(estadoStr.toUpperCase().trim());
        Estudiante estudiante = estudianteService.cambiarEstado(id, estado);
        return ResponseEntity.ok(estudiante);
    }
}
