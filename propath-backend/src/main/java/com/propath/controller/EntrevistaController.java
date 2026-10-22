
package com.propath.controller;

import com.propath.entity.Entrevista;
import com.propath.entity.Postulacion;
import com.propath.repository.EntrevistaRepository;
import com.propath.repository.PostulacionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/entrevistas")
public class EntrevistaController {

    private final EntrevistaRepository entrevistas;
    private final PostulacionRepository postulaciones;

    public EntrevistaController(
            EntrevistaRepository entrevistas,
            PostulacionRepository postulaciones) {
        this.entrevistas = entrevistas;
        this.postulaciones = postulaciones;
    }

    @GetMapping
    public List<Map<String, Object>> listar(
            @RequestParam(required = false) Integer idEmpresa,
            @RequestParam(required = false) Integer idEstudiante) {

        if ((idEmpresa == null) == (idEstudiante == null)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Indica idEmpresa o idEstudiante.");
        }

        List<Entrevista> lista = idEmpresa != null
                ? entrevistas.findByPostulacionOfertaEmpresaId(idEmpresa)
                : entrevistas.findByPostulacionEstudianteId(idEstudiante);

        return lista.stream().map(this::convertir).toList();
    }

    @GetMapping("/{id}")
    public Map<String, Object> obtener(@PathVariable Integer id) {
        return convertir(buscar(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> crear(@RequestBody Map<String, Object> datos) {
        Entrevista entrevista = new Entrevista();
        guardarDatos(entrevista, datos);

        Integer idPostulacion = entrevista.getPostulacion().getId();

        boolean yaExiste = entrevistas
                .findByPostulacionOfertaEmpresaId(
                        entrevista.getPostulacion().getOferta().getEmpresa().getId())
                .stream()
                .anyMatch(e -> idPostulacion.equals(e.getPostulacion().getId()));

        if (yaExiste) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Esta postulación ya tiene una entrevista. Puedes editarla o eliminarla.");
        }

        return convertir(entrevistas.save(entrevista));
    }

    @PutMapping("/{id}")
    public Map<String, Object> actualizar(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> datos) {

        Entrevista entrevista = buscar(id);
        guardarDatos(entrevista, datos);
        return convertir(entrevistas.save(entrevista));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Integer id) {
        Entrevista entrevista = buscar(id);
        entrevistas.delete(entrevista);
    }

    private Entrevista buscar(Integer id) {
        return entrevistas.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Entrevista no encontrada."));
    }

    private void guardarDatos(Entrevista e, Map<String, Object> datos) {
        try {
            Integer idPostulacion = Integer.valueOf(
                    datos.get("idPostulacion").toString());

            Postulacion postulacion = postulaciones.findById(idPostulacion)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Postulación no encontrada."));

            if (postulacion.getEstado() != Postulacion.Estado.SELECCIONADO) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "No se puede programar esta postulación.");
            }

            LocalDate fecha = LocalDate.parse(datos.get("fecha").toString());
            LocalTime hora = LocalTime.parse(datos.get("hora").toString());

            String modalidad = datos.get("modalidad").toString().trim().toUpperCase();
            String lugar = datos.get("enlaceLugar").toString().trim();
            String estado = datos.getOrDefault("estado", "PROGRAMADA")
                    .toString().trim().toUpperCase();
            String observaciones = datos.getOrDefault("observaciones", "")
                    .toString().trim();

            if (!modalidad.equals("VIRTUAL") && !modalidad.equals("PRESENCIAL")) {
                throw new IllegalArgumentException("Modalidad inválida.");
            }

            if (!List.of("PROGRAMADA", "REALIZADA", "CANCELADA").contains(estado)) {
                throw new IllegalArgumentException("Estado inválido.");
            }

            if (lugar.isBlank() || lugar.length() > 500
                    || observaciones.length() > 1000) {
                throw new IllegalArgumentException("Lugar u observaciones inválidos.");
            }

            if (modalidad.equals("VIRTUAL")) {
                URI uri = URI.create(lugar);
                if (!"https".equalsIgnoreCase(uri.getScheme())
                        || uri.getHost() == null
                        || uri.getUserInfo() != null) {
                    throw new IllegalArgumentException(
                            "La entrevista virtual requiere un enlace HTTPS válido.");
                }
            }

            e.setPostulacion(postulacion);
            e.setFecha(fecha);
            e.setHora(hora);
            e.setModalidad(modalidad);
            e.setEnlaceLugar(lugar);
            e.setEstado(estado);
            e.setObservaciones(observaciones);

        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Datos de entrevista inválidos: " + ex.getMessage());
        }
    }

    private Map<String, Object> convertir(Entrevista e) {
        Map<String, Object> datos = new java.util.LinkedHashMap<>();
        datos.put("id", e.getId());
        datos.put("idPostulacion", e.getPostulacion().getId());
        datos.put("idEstudiante", e.getPostulacion().getEstudiante().getId());
        datos.put("idEmpresa", e.getPostulacion().getOferta().getEmpresa().getId());
        datos.put("fecha", e.getFecha());
        datos.put("hora", e.getHora());
        datos.put("modalidad", e.getModalidad());
        datos.put("enlaceLugar", e.getEnlaceLugar());
        datos.put("estado", e.getEstado());
        datos.put("observaciones", e.getObservaciones());
        return datos;
    }
}
