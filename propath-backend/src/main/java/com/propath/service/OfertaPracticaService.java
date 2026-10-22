package com.propath.service;

import com.propath.entity.OfertaPractica;
import com.propath.repository.EmpresaRepository;
import com.propath.repository.OfertaPracticaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OfertaPracticaService {

    private final OfertaPracticaRepository ofertaPracticaRepository;
    private final EmpresaRepository empresaRepository;
    private final com.propath.repository.PostulacionRepository postulacionRepository;

    public OfertaPracticaService(OfertaPracticaRepository ofertaPracticaRepository,
                                 EmpresaRepository empresaRepository,
                                 com.propath.repository.PostulacionRepository postulacionRepository) {
        this.ofertaPracticaRepository = ofertaPracticaRepository;
        this.empresaRepository = empresaRepository;
        this.postulacionRepository = postulacionRepository;
    }

    @Transactional(readOnly = true)
    public List<OfertaPractica> listar() {
        return ofertaPracticaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public OfertaPractica buscarPorId(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID de oferta no puede ser nulo.");
        }
        return ofertaPracticaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Oferta de prácticas no encontrada con ID: " + id));
    }

    @Transactional
    public OfertaPractica guardar(OfertaPractica oferta) {
        if (oferta == null) {
            throw new IllegalArgumentException("La oferta no puede ser nula.");
        }
        if (oferta.getTitulo() == null || oferta.getTitulo().trim().isEmpty()) {
            throw new IllegalArgumentException("El título de la oferta es obligatorio.");
        }
        if (oferta.getDescripcion() == null || oferta.getDescripcion().trim().isEmpty()) {
            throw new IllegalArgumentException("La descripción de la oferta es obligatoria.");
        }
        if (oferta.getVacantes() == null || oferta.getVacantes() <= 0) {
            throw new IllegalArgumentException("El número de vacantes debe ser mayor a cero.");
        }
        if (oferta.getFechaLimite() == null) {
            throw new IllegalArgumentException("La fecha límite de postulación es obligatoria.");
        }
        if (oferta.getId() != null) {
            OfertaPractica existente = ofertaPracticaRepository.findById(oferta.getId())
                    .orElseThrow(() -> new IllegalArgumentException("No se puede actualizar. Oferta inexistente con ID: " + oferta.getId()));

            if (oferta.getEmpresa() == null || oferta.getEmpresa().getId() == null) {
                oferta.setEmpresa(existente.getEmpresa());
            }
            if (oferta.getEstado() == null) {
                oferta.setEstado(existente.getEstado());
            }
            if (oferta.getModalidad() == null) {
                oferta.setModalidad(existente.getModalidad());
            }
            if (oferta.getFechaPublicacion() == null) {
                oferta.setFechaPublicacion(existente.getFechaPublicacion());
            }
        }

        if (oferta.getEmpresa() == null || oferta.getEmpresa().getId() == null) {
            throw new IllegalArgumentException("La oferta debe pertenecer a una empresa registrada.");
        }

        Integer idEmpresa = oferta.getEmpresa().getId();
        com.propath.entity.Empresa empresa = empresaRepository.findById(idEmpresa)
                .orElseThrow(() -> new IllegalArgumentException("La empresa asociada (ID: " + idEmpresa + ") no existe."));

        if (empresa.getEstado() != com.propath.entity.Empresa.Estado.APROBADA) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN,
                    "La empresa no está aprobada (Estado: " + empresa.getEstado() + "). Únicamente empresas aprobadas pueden publicar o modificar ofertas.");
        }

        if (empresa.getUsuario() != null && empresa.getUsuario().getEstado() != com.propath.entity.Usuario.Estado.ACTIVO) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN,
                    "La cuenta de la empresa está inactiva. Únicamente cuentas activas pueden gestionar ofertas.");
        }

        return ofertaPracticaRepository.save(oferta);
    }

    @Transactional
    public void eliminar(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID de oferta no puede ser nulo.");
        }
        OfertaPractica oferta = ofertaPracticaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No se puede eliminar. Oferta inexistente con ID: " + id));

        if (oferta.getEmpresa() != null) {
            com.propath.entity.Empresa emp = oferta.getEmpresa();
            if (emp.getEstado() != com.propath.entity.Empresa.Estado.APROBADA) {
                throw new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.FORBIDDEN,
                        "Operación no permitida: la empresa se encuentra " + emp.getEstado() + ".");
            }
            if (emp.getUsuario() != null && emp.getUsuario().getEstado() != com.propath.entity.Usuario.Estado.ACTIVO) {
                throw new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.FORBIDDEN,
                        "Operación no permitida: la cuenta de la empresa está inactiva.");
            }
        }

        List<com.propath.entity.Postulacion> postulaciones = postulacionRepository.findByOfertaId(id);
        if (!postulaciones.isEmpty()) {
            postulacionRepository.deleteAll(postulaciones);
        }
        ofertaPracticaRepository.deleteById(id);
    }
}
