package com.propath.service;

import com.propath.entity.Empresa;
import com.propath.entity.Usuario;
import com.propath.repository.EmpresaRepository;
import com.propath.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class EmpresaService {

    private final EmpresaRepository empresaRepository;
    private final UsuarioRepository usuarioRepository;
    private final com.propath.repository.OfertaPracticaRepository ofertaPracticaRepository;
    private final com.propath.repository.PostulacionRepository postulacionRepository;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public EmpresaService(EmpresaRepository empresaRepository,
            UsuarioRepository usuarioRepository,
            com.propath.repository.OfertaPracticaRepository ofertaPracticaRepository,
            com.propath.repository.PostulacionRepository postulacionRepository) {
        this.empresaRepository = empresaRepository;
        this.usuarioRepository = usuarioRepository;
        this.ofertaPracticaRepository = ofertaPracticaRepository;
        this.postulacionRepository = postulacionRepository;
    }

    @Transactional(readOnly = true)
    public List<Empresa> listar() {
        return empresaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Empresa buscarPorId(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID de empresa no puede ser nulo.");
        }
        return empresaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada con ID: " + id));
    }

    @Transactional
    public Empresa guardar(Empresa empresa) {
        if (empresa == null) {
            throw new IllegalArgumentException("La empresa no puede ser nula.");
        }

        if (empresa.getId() != null) {
            Empresa existente = empresaRepository.findById(empresa.getId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "No se puede actualizar. Empresa inexistente con ID: " + empresa.getId()));

            Usuario u = existente.getUsuario();
            if (empresa.getUsuario() != null && u != null) {
                Usuario datosUsuario = empresa.getUsuario();
                if (datosUsuario.getNombre() != null && !datosUsuario.getNombre().trim().isEmpty()) {
                    u.setNombre(datosUsuario.getNombre().trim());
                }
                if (datosUsuario.getApellidos() != null) {
                    u.setApellidos(datosUsuario.getApellidos().trim());
                }
                if (datosUsuario.getCorreo() != null && !datosUsuario.getCorreo().trim().isEmpty()) {
                    String nuevoCorreo = datosUsuario.getCorreo().trim();
                    if (u.getCorreo() == null || !u.getCorreo().equalsIgnoreCase(nuevoCorreo)) {
                        usuarioRepository.findByCorreo(nuevoCorreo).ifPresent(otro -> {
                            if (!otro.getId().equals(u.getId())) {
                                throw new ResponseStatusException(HttpStatus.CONFLICT,
                                        "El correo electrónico ya está registrado por otro usuario.");
                            }
                        });
                        u.setCorreo(nuevoCorreo);
                    }
                }
                if (datosUsuario.getContrasena() != null && !datosUsuario.getContrasena().trim().isEmpty()) {
                    String clave = datosUsuario.getContrasena().trim();
                    if (!clave.startsWith("$2a$") && !clave.startsWith("$2b$") && !clave.startsWith("$2y$")) {
                        u.setContrasena(passwordEncoder.encode(clave));
                    }
                }
                usuarioRepository.save(u);
            }

            if (empresa.getRazonSocial() != null && !empresa.getRazonSocial().trim().isEmpty()) {
                existente.setRazonSocial(empresa.getRazonSocial().trim());
            }
            if (empresa.getRuc() != null) {
                String nuevoRuc = empresa.getRuc().trim();

                if (!nuevoRuc.matches("\\d{11}")) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "El RUC debe contener exactamente 11 dígitos numéricos.");
                }

                boolean rucDuplicado = empresaRepository.findAll().stream()
                        .anyMatch(otra -> !otra.getId().equals(existente.getId())
                                && nuevoRuc.equals(otra.getRuc()));

                if (rucDuplicado) {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "El RUC ya está registrado por otra empresa.");
                }

                existente.setRuc(nuevoRuc);
            }
            if (empresa.getEstado() != null) {
                existente.setEstado(empresa.getEstado());
            }
            if (empresa.getDireccion() != null) {
                existente.setDireccion(empresa.getDireccion().trim());
            }
            if (empresa.getDescripcion() != null) {
                existente.setDescripcion(empresa.getDescripcion().trim());
            }
            if (empresa.getTelefono() != null) {
                existente.setTelefono(empresa.getTelefono().trim());
            }

            return empresaRepository.save(existente);
        }

        if (empresa.getRazonSocial() == null || empresa.getRazonSocial().trim().isEmpty()) {
            throw new IllegalArgumentException("La razón social es obligatoria.");
        }
        if (empresa.getRuc() == null || !empresa.getRuc().trim().matches("\\d{11}")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El RUC debe contener exactamente 11 dígitos numéricos.");
        }

        boolean rucDuplicado = empresaRepository.findAll().stream()
                .anyMatch(otra -> empresa.getRuc().trim().equals(otra.getRuc()));

        if (rucDuplicado) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El RUC ya está registrado por otra empresa.");
        }

        if (empresa.getUsuario() == null || empresa.getUsuario().getId() == null) {
            throw new IllegalArgumentException("La empresa debe estar vinculada a una cuenta de usuario existente.");
        }

        Integer idUsuario = empresa.getUsuario().getId();
        if (!usuarioRepository.existsById(idUsuario)) {
            throw new IllegalArgumentException("La cuenta de usuario vinculada (ID: " + idUsuario + ") no existe.");
        }

        return empresaRepository.save(empresa);
    }

    @Transactional
    public Empresa cambiarEstado(Integer id, Empresa.Estado nuevoEstado) {
        if (id == null) {
            throw new IllegalArgumentException("El ID de empresa no puede ser nulo.");
        }
        if (nuevoEstado == null) {
            throw new IllegalArgumentException("El nuevo estado no puede ser nulo.");
        }
        Empresa empresa = empresaRepository.findById(id)
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Empresa no encontrada con ID: " + id));
        empresa.setEstado(nuevoEstado);
        return empresaRepository.save(empresa);
    }

    @Transactional
    public Empresa cambiarEstadoCuenta(Integer id, Usuario.Estado nuevoEstadoCuenta) {
        if (id == null) {
            throw new IllegalArgumentException("El ID de empresa no puede ser nulo.");
        }
        if (nuevoEstadoCuenta == null) {
            throw new IllegalArgumentException("El nuevo estado de cuenta no puede ser nulo.");
        }
        Empresa empresa = empresaRepository.findById(id)
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Empresa no encontrada con ID: " + id));
        Usuario usuario = empresa.getUsuario();
        if (usuario == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "La empresa no tiene un usuario asociado.");
        }
        usuario.setEstado(nuevoEstadoCuenta);
        usuarioRepository.save(usuario);
        return empresa;
    }

    @Transactional
    public void eliminar(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID de empresa no puede ser nulo.");
        }
        Empresa empresa = empresaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No se puede eliminar. Empresa inexistente con ID: " + id));

        Usuario usuario = empresa.getUsuario();
        if (usuario != null && usuario.getEstado() == Usuario.Estado.ACTIVO) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "No se puede eliminar una empresa con cuenta ACTIVA. Debe estar dada de baja (INACTIVO) primero.");
        }

        List<com.propath.entity.OfertaPractica> ofertas = ofertaPracticaRepository.findByEmpresaId(id);
        if (!ofertas.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No se puede eliminar la empresa porque tiene "
                    + ofertas.size()
                    + " oferta(s) de prácticas registrada(s). Debe gestionar o archivar las ofertas antes de eliminar la empresa.");
        }

        empresaRepository.delete(empresa);
        if (usuario != null) {
            usuarioRepository.delete(usuario);
        }
    }
}
