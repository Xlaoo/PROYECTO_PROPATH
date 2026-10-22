package com.propath.controller;

import com.propath.entity.OfertaPractica;
import com.propath.service.OfertaPracticaService;
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
@RequestMapping("/api/ofertas")
public class OfertaPracticaController {

    private final OfertaPracticaService ofertaPracticaService;

    public OfertaPracticaController(OfertaPracticaService ofertaPracticaService) {
        this.ofertaPracticaService = ofertaPracticaService;
    }

    @GetMapping
    public ResponseEntity<List<OfertaPractica>> listar() {
        return ResponseEntity.ok(ofertaPracticaService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OfertaPractica> buscarPorId(@PathVariable("id") Integer id) {
        return ResponseEntity.ok(ofertaPracticaService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<OfertaPractica> registrar(@RequestBody OfertaPractica oferta) {
        OfertaPractica creada = ofertaPracticaService.guardar(oferta);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OfertaPractica> actualizar(
            @PathVariable("id") Integer id,
            @RequestBody OfertaPractica oferta) {

        OfertaPractica actual = ofertaPracticaService.buscarPorId(id);

        oferta.setId(id);
        oferta.setEmpresa(actual.getEmpresa());
        oferta.setFechaPublicacion(actual.getFechaPublicacion());

        oferta.setEstado(actual.getEstado());

        OfertaPractica actualizada = ofertaPracticaService.guardar(oferta);
        return ResponseEntity.ok(actualizada);
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<OfertaPractica> actualizarEstado(
            @PathVariable("id") Integer id,
            @RequestBody java.util.Map<String, String> datos) {

        OfertaPractica oferta = ofertaPracticaService.buscarPorId(id);

        String nuevoEstado = datos.get("estado");

        if (nuevoEstado == null || nuevoEstado.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        nuevoEstado = nuevoEstado.trim().toUpperCase();

        if (!java.util.Set.of(
                "PENDIENTE", "ACTIVA", "RECHAZADA",
                "CERRADA", "INACTIVA").contains(nuevoEstado)) {
            return ResponseEntity.badRequest().build();
        }

        oferta.setEstado(OfertaPractica.Estado.valueOf(nuevoEstado));
        return ResponseEntity.ok(ofertaPracticaService.guardar(oferta));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable("id") Integer id) {
        ofertaPracticaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
