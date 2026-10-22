package com.propath.service;

import com.propath.entity.Estudiante;
import com.propath.repository.EstudianteRepository;
import com.propath.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EstudianteService {

    private final EstudianteRepository estudianteRepository;
    private final UsuarioRepository usuarioRepository;
    private final com.propath.repository.PostulacionRepository postulacionRepository;

    public EstudianteService(EstudianteRepository estudianteRepository,
                             UsuarioRepository usuarioRepository,
                             com.propath.repository.PostulacionRepository postulacionRepository) {
        this.estudianteRepository = estudianteRepository;
        this.usuarioRepository = usuarioRepository;
        this.postulacionRepository = postulacionRepository;
    }

    @Transactional(readOnly = true)
    public List<Estudiante> listar() {
        return estudianteRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Estudiante buscarPorId(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID de estudiante no puede ser nulo.");
        }
        return estudianteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Estudiante no encontrado con ID: " + id));
    }

    @Transactional
    public Estudiante guardar(Estudiante estudiante) {
        if (estudiante == null) {
            throw new IllegalArgumentException("El estudiante no puede ser nulo.");
        }
        if (estudiante.getApellidos() == null || estudiante.getApellidos().trim().isEmpty()) {
            throw new IllegalArgumentException("Los apellidos del estudiante son obligatorios.");
        }

        if (estudiante.getId() != null) {
            Estudiante existente = estudianteRepository.findById(estudiante.getId())
                    .orElseThrow(() -> new IllegalArgumentException("No se puede actualizar. Estudiante inexistente con ID: " + estudiante.getId()));

            com.propath.entity.Usuario u = existente.getUsuario();
            if (u != null) {
                if (estudiante.getUsuario() != null && estudiante.getUsuario().getNombre() != null && !estudiante.getUsuario().getNombre().trim().isEmpty()) {
                    u.setNombre(estudiante.getUsuario().getNombre().trim());
                }
                if (estudiante.getApellidos() != null && !estudiante.getApellidos().trim().isEmpty()) {
                    u.setApellidos(estudiante.getApellidos().trim());
                }
                usuarioRepository.save(u);
            }

            if (estudiante.getUsuario() == null || estudiante.getUsuario().getId() == null) {
                estudiante.setUsuario(existente.getUsuario());
            }
        }

        if (estudiante.getUsuario() == null || estudiante.getUsuario().getId() == null) {
            throw new IllegalArgumentException("El estudiante debe estar vinculado a una cuenta de usuario existente.");
        }

        Integer idUsuario = estudiante.getUsuario().getId();
        if (!usuarioRepository.existsById(idUsuario)) {
            throw new IllegalArgumentException("La cuenta de usuario vinculada (ID: " + idUsuario + ") no existe.");
        }

        return estudianteRepository.save(estudiante);
    }

    @Transactional
    public void eliminar(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID de estudiante no puede ser nulo.");
        }
        if (!estudianteRepository.existsById(id)) {
            throw new IllegalArgumentException("No se puede eliminar. Estudiante inexistente con ID: " + id);
        }

        List<com.propath.entity.Postulacion> postulaciones = postulacionRepository.findByEstudianteId(id);
        if (!postulaciones.isEmpty()) {
            postulacionRepository.deleteAll(postulaciones);
        }
        estudianteRepository.deleteById(id);
    }

    @Transactional
    public Estudiante cambiarEstado(Integer idEstudiante, com.propath.entity.Usuario.Estado nuevoEstado) {
        if (idEstudiante == null) {
            throw new IllegalArgumentException("El ID de estudiante no puede ser nulo.");
        }
        if (nuevoEstado == null) {
            throw new IllegalArgumentException("El nuevo estado no puede ser nulo.");
        }
        Estudiante estudiante = buscarPorId(idEstudiante);
        com.propath.entity.Usuario u = estudiante.getUsuario();
        if (u != null) {
            u.setEstado(nuevoEstado);
            usuarioRepository.save(u);
        }
        return estudiante;
    }
}
