package com.propath.controller;

import com.propath.entity.Postulacion;
import com.propath.service.PostulacionService;
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
import java.util.Map;
import java.util.List;
import com.propath.repository.EntrevistaRepository;

@RestController
@RequestMapping("/api/postulaciones")
public class PostulacionController {

    private final PostulacionService postulacionService;
    private final EntrevistaRepository entrevistaRepository;

    public PostulacionController(
            PostulacionService postulacionService,
            EntrevistaRepository entrevistaRepository) {
        this.postulacionService = postulacionService;
        this.entrevistaRepository = entrevistaRepository;
    }

    @GetMapping
    public ResponseEntity<List<Postulacion>> listar() {
        return ResponseEntity.ok(postulacionService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Postulacion> buscarPorId(@PathVariable("id") Integer id) {
        return ResponseEntity.ok(postulacionService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<Postulacion> registrar(
            @RequestBody Map<String, Object> datos) {

        try {
            Map<?, ?> estudiante = (Map<?, ?>) datos.get("estudiante");

            Map<?, ?> oferta = (Map<?, ?>) datos.get("oferta");

            Integer idEstudiante = ((Number) estudiante.get("id")).intValue();

            Integer idOferta = ((Number) oferta.get("id")).intValue();

            @SuppressWarnings("unchecked")
            List<String> respuestas = (List<String>) datos.get("respuestas");

            Postulacion creada = postulacionService.registrarConRespuestas(
                    idEstudiante, idOferta, respuestas);

            return ResponseEntity.status(HttpStatus.CREATED).body(creada);

        } catch (ClassCastException | NullPointerException
                | NumberFormatException e) {

            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Datos de postulación inválidos.");
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Postulacion> actualizar(
            @PathVariable("id") Integer id,
            @RequestBody Postulacion postulacion) {

        Postulacion actual = postulacionService.buscarPorId(id);

        if (postulacion.getEstado() == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Debe indicar el estado de la postulación.");
        }

        if (actual.getEstado() == Postulacion.Estado.NO_SELECCIONADO) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Esta postulación ya fue rechazada definitivamente.");
        }

        if (actual.getEstado() == Postulacion.Estado.SELECCIONADO
                && postulacion.getEstado() != Postulacion.Estado.NO_SELECCIONADO) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Un postulante seleccionado solo puede ser rechazado.");
        }

        if (postulacion.getEstado() == Postulacion.Estado.NO_SELECCIONADO) {

            if (entrevistaRepository.existsByPostulacionId(id)) {
                throw new org.springframework.web.server.ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "No puedes rechazar al postulante porque tiene una entrevista registrada.");
            }

            String motivo = postulacion.getMotivoRechazo();

            if (motivo == null || motivo.trim().isEmpty()
                    || motivo.trim().length() > 1000) {
                throw new org.springframework.web.server.ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Debes ingresar un motivo de rechazo válido.");
            }

            actual.setMotivoRechazo(motivo.trim());
        }

        actual.setEstado(postulacion.getEstado());

        Postulacion actualizada = postulacionService.guardar(actual);

        if (postulacion.getEstado() == Postulacion.Estado.NO_SELECCIONADO
                && (actualizada.getMotivoRechazo() == null
                        || actualizada.getMotivoRechazo().trim().isEmpty())) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "No se pudo guardar el motivo de rechazo.");
        }

        return ResponseEntity.ok(actualizada);
    }

    @PutMapping("/{id}/respuestas")
    public ResponseEntity<Postulacion> actualizarRespuestas(
            @PathVariable("id") Integer id,
            @RequestBody Map<String, Object> datos) {

        try {
            Number numeroEstudiante = (Number) datos.get("idEstudiante");

            if (numeroEstudiante == null) {
                throw new IllegalArgumentException();
            }

            Object valor = datos.get("respuestas");

            if (!(valor instanceof List<?>)) {
                throw new IllegalArgumentException();
            }

            List<?> lista = (List<?>) valor;

            if (lista.size() != 12) {
                throw new IllegalArgumentException();
            }

            List<String> respuestas = new java.util.ArrayList<>();

            for (Object elemento : lista) {
                if (!(elemento instanceof String)) {
                    throw new IllegalArgumentException();
                }

                String respuesta = ((String) elemento).trim();

                if (respuesta.isEmpty() || respuesta.length() > 1000) {
                    throw new IllegalArgumentException();
                }

                respuestas.add(respuesta);
            }

            Postulacion actualizada = postulacionService.actualizarRespuestas(
                    id,
                    numeroEstudiante.intValue(),
                    respuestas);

            return ResponseEntity.ok(actualizada);

        } catch (IllegalArgumentException | ClassCastException e) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Debes proporcionar 12 respuestas válidas.");
        }
    }

    @GetMapping("/{id}/respuestas")
    public ResponseEntity<List<Map<String, Object>>> listarRespuestas(
            @PathVariable("id") Integer id,
            @org.springframework.web.bind.annotation.RequestParam("idEstudiante") Integer idEstudiante) {

        List<Map<String, Object>> resultado = new java.util.ArrayList<>();

        for (com.propath.entity.RespuestaPostulacion registro : postulacionService.obtenerRespuestas(id,
                idEstudiante)) {

            Map<String, Object> item = new java.util.LinkedHashMap<>();
            item.put("numeroPregunta", registro.getNumeroPregunta());
            item.put("respuesta", registro.getRespuesta());
            resultado.add(item);
        }

        return ResponseEntity.ok(resultado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable("id") Integer id) {
        postulacionService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
