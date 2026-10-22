package com.propath.service;

import com.propath.entity.OfertaPractica;
import com.propath.entity.Postulacion;
import com.propath.repository.EstudianteRepository;
import com.propath.repository.OfertaPracticaRepository;
import com.propath.repository.PostulacionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.propath.repository.DocumentoEstudianteRepository;
import java.util.List;
import java.util.Arrays;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import com.propath.entity.RespuestaPostulacion;
import com.propath.repository.RespuestaPostulacionRepository;
import java.util.ArrayList;
import java.util.Arrays;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PostulacionService {

    private final PostulacionRepository postulacionRepository;
    private final EstudianteRepository estudianteRepository;
    private final OfertaPracticaRepository ofertaPracticaRepository;
    private final DocumentoEstudianteRepository documentoEstudianteRepository;
    private final RespuestaPostulacionRepository respuestaRepository;

    public PostulacionService(PostulacionRepository postulacionRepository,
            EstudianteRepository estudianteRepository,
            OfertaPracticaRepository ofertaPracticaRepository,
            DocumentoEstudianteRepository documentoEstudianteRepository,
            RespuestaPostulacionRepository respuestaRepository) {

        this.postulacionRepository = postulacionRepository;
        this.estudianteRepository = estudianteRepository;
        this.ofertaPracticaRepository = ofertaPracticaRepository;
        this.documentoEstudianteRepository = documentoEstudianteRepository;
        this.respuestaRepository = respuestaRepository;

    }

    private static final String[] PREGUNTAS = {
            "¿Por qué te interesa esta oportunidad?",
            "¿Qué conocimientos puedes aportar?",
            "¿Qué herramientas o tecnologías manejas?",
            "¿Has desarrollado proyectos relacionados?",
            "¿Qué esperas aprender?",
            "¿Cuál es tu disponibilidad horaria?",
            "¿Cuáles son tus principales fortalezas?",
            "¿Cómo organizas tus responsabilidades?",
            "¿Cómo reaccionas ante un problema inesperado?",
            "¿Cómo trabajas en equipo?",
            "¿Qué aspecto profesional deseas mejorar?",
            "¿Cuáles son tus objetivos profesionales?"
    };

    @Transactional
    public Postulacion registrarConRespuestas(
            Integer idEstudiante,
            Integer idOferta,
            List<String> respuestas) {

        if (idEstudiante == null || idOferta == null ||
                respuestas == null || respuestas.size() != 12) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Debes completar las 12 preguntas.");
        }

        for (String respuesta : respuestas) {
            if (respuesta == null || respuesta.trim().isEmpty()
                    || respuesta.length() > 1000) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Todas las respuestas son obligatorias y deben tener máximo 1000 caracteres.");
            }
        }

        // guardar() mantiene las validaciones existentes:
        // CV, oferta activa, empresa aprobada, duplicados y vacantes.
        Postulacion nueva = new Postulacion();

        com.propath.entity.Estudiante estudiante = estudianteRepository.findById(idEstudiante)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Estudiante no encontrado."));

        OfertaPractica oferta = ofertaPracticaRepository.findById(idOferta)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Oferta no encontrada."));

        nueva.setEstudiante(estudiante);
        nueva.setOferta(oferta);
        nueva.setEstado(Postulacion.Estado.POSTULADO);

        Postulacion guardada = guardar(nueva);

        List<RespuestaPostulacion> registros = new ArrayList<>();

        for (int i = 0; i < PREGUNTAS.length; i++) {
            RespuestaPostulacion registro = new RespuestaPostulacion();

            registro.setPostulacion(guardada);
            registro.setNumeroPregunta(i + 1);
            registro.setPregunta(PREGUNTAS[i]);
            registro.setRespuesta(respuestas.get(i).trim());

            registros.add(registro);
        }

        respuestaRepository.saveAll(registros);

        return guardada;
    }

    @Transactional(readOnly = true)
    public List<Postulacion> listar() {
        return postulacionRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Postulacion buscarPorId(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID de postulación no puede ser nulo.");
        }
        return postulacionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Postulación no encontrada con ID: " + id));
    }

    @Transactional
    public Postulacion guardar(Postulacion postulacion) {
        if (postulacion == null) {
            throw new IllegalArgumentException("La postulación no puede ser nula.");
        }
        if (postulacion.getEstudiante() == null || postulacion.getEstudiante().getId() == null) {
            throw new IllegalArgumentException("La postulación debe especificar un estudiante válido.");
        }
        if (postulacion.getOferta() == null || postulacion.getOferta().getId() == null) {
            throw new IllegalArgumentException("La postulación debe especificar una oferta de prácticas válida.");
        }

        Integer idEstudiante = postulacion.getEstudiante().getId();
        Integer idOferta = postulacion.getOferta().getId();

        if (!estudianteRepository.existsById(idEstudiante)) {
            throw new IllegalArgumentException("El estudiante con ID " + idEstudiante + " no existe.");
        }
        if (!ofertaPracticaRepository.existsById(idOferta)) {
            throw new IllegalArgumentException("La oferta de prácticas con ID " + idOferta + " no existe.");
        }
        if (postulacion.getId() == null) {
            if (documentoEstudianteRepository
                    .findByEstudianteIdAndTipoDocumento(idEstudiante, "CV")
                    .isEmpty()) {

                throw new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.FORBIDDEN,
                        "Debes registrar tu currículum vitae en PDF antes de postular.");
            }
            OfertaPractica oferta = ofertaPracticaRepository.findById(idOferta)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "La oferta de prácticas no existe."));

            if (oferta.getEstado() != OfertaPractica.Estado.ACTIVA) {
                throw new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.FORBIDDEN,
                        "No puedes postular a esta oferta porque todavía no está ACTIVA.");
            }
            long vacantesOcupadas = postulacionRepository.countByOfertaIdAndEstadoIn(
                    idOferta,
                    Arrays.asList(
                            Postulacion.Estado.POSTULADO,
                            Postulacion.Estado.EN_REVISION,
                            Postulacion.Estado.SELECCIONADO));

            if (oferta.getVacantes() == null ||
                    vacantesOcupadas >= oferta.getVacantes()) {

                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Esta oferta ya no tiene vacantes disponibles.");
            }
            if (oferta.getEmpresa() == null ||
                    oferta.getEmpresa().getEstado() != com.propath.entity.Empresa.Estado.APROBADA) {

                throw new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.FORBIDDEN,
                        "No puedes postular porque la empresa no está aprobada.");
            }
        }

        boolean existeDuplicado;

        if (postulacion.getId() == null) {
            existeDuplicado = postulacionRepository
                    .existsByEstudianteIdAndOfertaId(idEstudiante, idOferta);
        } else {
            existeDuplicado = postulacionRepository.findAll().stream()
                    .anyMatch(p -> p.getEstudiante() != null
                            && idEstudiante.equals(p.getEstudiante().getId())
                            && p.getOferta() != null
                            && idOferta.equals(p.getOferta().getId())
                            && !postulacion.getId().equals(p.getId()));
        }

        if (existeDuplicado) {
            throw new IllegalStateException("El estudiante ya cuenta con una postulación registrada para esta oferta.");
        }

        if (postulacion.getId() != null) {
            if (!postulacionRepository.existsById(postulacion.getId())) {
                throw new IllegalArgumentException(
                        "No se puede actualizar. Postulación inexistente con ID: " + postulacion.getId());
            }
            OfertaPractica of = ofertaPracticaRepository.findById(idOferta).orElse(null);
            if (of != null && of.getEmpresa() != null) {
                com.propath.entity.Empresa emp = of.getEmpresa();
                if (emp.getEstado() != com.propath.entity.Empresa.Estado.APROBADA) {
                    throw new org.springframework.web.server.ResponseStatusException(
                            org.springframework.http.HttpStatus.FORBIDDEN,
                            "Operación no permitida: la empresa asociada no está aprobada (Estado: " + emp.getEstado()
                                    + ").");
                }
                if (emp.getUsuario() != null
                        && emp.getUsuario().getEstado() != com.propath.entity.Usuario.Estado.ACTIVO) {
                    throw new org.springframework.web.server.ResponseStatusException(
                            org.springframework.http.HttpStatus.FORBIDDEN,
                            "Operación no permitida: la cuenta de la empresa asociada está inactiva.");
                }
            }
        }

        return postulacionRepository.save(postulacion);
    }

    @Transactional
    public Postulacion actualizarRespuestas(
            Integer idPostulacion,
            Integer idEstudiante,
            List<String> respuestas) {

        if (idEstudiante == null || idEstudiante <= 0
                || respuestas == null || respuestas.size() != 12) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Datos de actualización inválidos.");
        }

        Postulacion postulacion = postulacionRepository.findById(idPostulacion)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Postulación no encontrada."));

        if (!postulacion.getEstudiante().getId().equals(idEstudiante)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Esta postulación no pertenece al estudiante.");
        }

        if (postulacion.getEstado() != Postulacion.Estado.POSTULADO) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Solo puedes editar postulaciones en estado POSTULADO.");
        }

        List<RespuestaPostulacion> registros = respuestaRepository
                .findByPostulacionIdOrderByNumeroPreguntaAsc(idPostulacion);

        if (registros.size() != 12) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "La postulación no tiene las 12 respuestas registradas.");
        }

        for (int i = 0; i < 12; i++) {
            String respuesta = respuestas.get(i);

            if (respuesta == null || respuesta.trim().isEmpty()
                    || respuesta.trim().length() > 1000) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Las 12 respuestas son obligatorias.");
            }

            registros.get(i).setRespuesta(respuesta.trim());
        }

        respuestaRepository.saveAll(registros);

        return postulacion;
    }

    @Transactional(readOnly = true)
    public List<RespuestaPostulacion> obtenerRespuestas(
            Integer idPostulacion,
            Integer idEstudiante) {

        Postulacion postulacion = postulacionRepository.findById(idPostulacion)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Postulación no encontrada."));

        if (idEstudiante == null
                || !postulacion.getEstudiante().getId().equals(idEstudiante)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "No puedes consultar esta postulación.");
        }

        return respuestaRepository
                .findByPostulacionIdOrderByNumeroPreguntaAsc(idPostulacion);
    }

    @Transactional
    public void eliminar(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID de postulación no puede ser nulo.");
        }
        if (!postulacionRepository.existsById(id)) {
            throw new IllegalArgumentException("No se puede eliminar. Postulación inexistente con ID: " + id);
        }
        postulacionRepository.deleteById(id);
    }
}
