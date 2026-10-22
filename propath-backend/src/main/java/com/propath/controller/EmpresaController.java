package com.propath.controller;

import com.propath.entity.Empresa;
import com.propath.service.EmpresaService;
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
@RequestMapping("/api/empresas")
public class EmpresaController {

    private final EmpresaService empresaService;

    public EmpresaController(EmpresaService empresaService) {
        this.empresaService = empresaService;
    }

    @GetMapping
    public ResponseEntity<List<Empresa>> listar() {
        return ResponseEntity.ok(empresaService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Empresa> buscarPorId(@PathVariable("id") Integer id) {
        return ResponseEntity.ok(empresaService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<Empresa> registrar(@RequestBody Empresa empresa) {
        Empresa creada = empresaService.guardar(empresa);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Empresa> actualizar(@PathVariable("id") Integer id, @RequestBody Empresa empresa) {
        empresa.setId(id);
        Empresa actualizada = empresaService.guardar(empresa);
        return ResponseEntity.ok(actualizada);
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<Empresa> cambiarEstado(@PathVariable("id") Integer id,
                                                 @RequestBody(required = false) java.util.Map<String, String> body,
                                                 @org.springframework.web.bind.annotation.RequestParam(value = "nuevoEstado", required = false) String paramEstado) {
        String estadoStr = (body != null && body.containsKey("estado")) ? body.get("estado") : paramEstado;
        Empresa.Estado nuevoEstado = Empresa.Estado.fromString(estadoStr);
        if (nuevoEstado == null) {
            return ResponseEntity.badRequest().build();
        }
        Empresa actualizada = empresaService.cambiarEstado(id, nuevoEstado);
        return ResponseEntity.ok(actualizada);
    }

    @PutMapping("/{id}/estado-cuenta")
    public ResponseEntity<Empresa> cambiarEstadoCuenta(@PathVariable("id") Integer id,
                                                        @RequestBody(required = false) java.util.Map<String, String> body,
                                                        @org.springframework.web.bind.annotation.RequestParam(value = "nuevoEstado", required = false) String paramEstado) {
        String estadoStr = (body != null && body.containsKey("estado")) ? body.get("estado") : paramEstado;
        if (estadoStr == null || estadoStr.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        com.propath.entity.Usuario.Estado nuevoEstado = com.propath.entity.Usuario.Estado.valueOf(estadoStr.trim().toUpperCase());
        Empresa actualizada = empresaService.cambiarEstadoCuenta(id, nuevoEstado);
        return ResponseEntity.ok(actualizada);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable("id") Integer id) {
        empresaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
