package com.propath.service;

import com.propath.entity.Administrador;
import com.propath.entity.Empresa;
import com.propath.entity.Estudiante;
import com.propath.entity.OfertaPractica;
import com.propath.entity.Postulacion;
import com.propath.entity.Usuario;
import com.propath.repository.AdministradorRepository;
import com.propath.repository.EmpresaRepository;
import com.propath.repository.EstudianteRepository;
import com.propath.repository.OfertaPracticaRepository;
import com.propath.repository.PostulacionRepository;
import com.propath.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final EstudianteRepository estudianteRepository;
    private final EmpresaRepository empresaRepository;
    private final AdministradorRepository administradorRepository;
    private final OfertaPracticaRepository ofertaPracticaRepository;
    private final PostulacionRepository postulacionRepository;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UsuarioService(UsuarioRepository usuarioRepository,
                          EstudianteRepository estudianteRepository,
                          EmpresaRepository empresaRepository,
                          AdministradorRepository administradorRepository,
                          OfertaPracticaRepository ofertaPracticaRepository,
                          PostulacionRepository postulacionRepository) {
        this.usuarioRepository = usuarioRepository;
        this.estudianteRepository = estudianteRepository;
        this.empresaRepository = empresaRepository;
        this.administradorRepository = administradorRepository;
        this.ofertaPracticaRepository = ofertaPracticaRepository;
        this.postulacionRepository = postulacionRepository;
    }

    @Transactional(readOnly = true)
    public List<Usuario> listar() {
        return usuarioRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Usuario buscarPorId(Integer id) {
        if (id == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El ID de usuario no puede ser nulo.");
        }
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado con ID: " + id));
    }

    @Transactional
    public Usuario guardar(Usuario usuario) {
        if (usuario == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El usuario no puede ser nulo.");
        }
        if (usuario.getCorreo() == null || usuario.getCorreo().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El correo electrónico es obligatorio.");
        }
        if (usuario.getNombre() == null || usuario.getNombre().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre de usuario es obligatorio.");
        }

        if (usuario.getId() != null) {
            Usuario existente = usuarioRepository.findById(usuario.getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No se puede actualizar. Usuario inexistente con ID: " + usuario.getId()));

            boolean esAdminPrincipal = existente.esAdminPrincipal() || (existente.getId() != null && existente.getId() == 60);

            if (esAdminPrincipal) {
                if (usuario.getEstado() != null && usuario.getEstado() != Usuario.Estado.ACTIVO) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No está permitido desactivar o bloquear la cuenta del administrador principal del sistema.");
                }
                if (usuario.getRol() != null && usuario.getRol() != Usuario.Rol.ADMIN) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No está permitido modificar el rol del administrador principal del sistema.");
                }
            }

            if (existente.getRol() != null && usuario.getRol() != null && existente.getRol() != usuario.getRol()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No está permitido cambiar el rol de un usuario existente.");
            }

            if (usuario.getRol() == null) {
                usuario.setRol(existente.getRol());
            }

            if (!existente.getCorreo().equalsIgnoreCase(usuario.getCorreo().trim())) {
                usuarioRepository.findByCorreo(usuario.getCorreo().trim()).ifPresent(otro -> {
                    if (!otro.getId().equals(usuario.getId())) {
                        throw new ResponseStatusException(HttpStatus.CONFLICT, "El correo electrónico ya está registrado por otro usuario.");
                    }
                });
            }

            if (usuario.getContrasena() != null && !usuario.getContrasena().trim().isEmpty()) {
                String clave = usuario.getContrasena().trim();
                if (!clave.startsWith("$2a$") && !clave.startsWith("$2b$") && !clave.startsWith("$2y$")) {
                    usuario.setContrasena(passwordEncoder.encode(clave));
                }
            } else {
                usuario.setContrasena(existente.getContrasena());
            }

            if (usuario.getApellidos() == null) {
                usuario.setApellidos(existente.getApellidos());
            }

            if (usuario.getEstado() == null) {
                usuario.setEstado(existente.getEstado());
            }

            Usuario guardado = usuarioRepository.save(usuario);

            if (guardado.getRol() == Usuario.Rol.ESTUDIANTE && guardado.getApellidos() != null) {
                estudianteRepository.findByUsuarioId(guardado.getId()).ifPresent(est -> {
                    est.setApellidos(guardado.getApellidos());
                    estudianteRepository.save(est);
                });
            }

            if (guardado.getRol() == Usuario.Rol.ADMIN) {
                administradorRepository.findByUsuarioId(guardado.getId()).orElseGet(() -> {
                    Administrador admin = new Administrador();
                    admin.setUsuario(guardado);
                    admin.setCargo(guardado.getId() != null && guardado.getId() == 60 ? "Administrador Principal" : "Administrador");
                    return administradorRepository.save(admin);
                });
            }

            return guardado;
        } else {
            if (usuario.getRol() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El rol del usuario es obligatorio.");
            }
            if (usuarioRepository.findByCorreo(usuario.getCorreo().trim()).isPresent()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "El correo electrónico ya está registrado.");
            }
            if (usuario.getContrasena() == null || usuario.getContrasena().trim().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contraseña es obligatoria para nuevos usuarios.");
            }
            String clave = usuario.getContrasena().trim();
            if (!clave.startsWith("$2a$") && !clave.startsWith("$2b$") && !clave.startsWith("$2y$")) {
                usuario.setContrasena(passwordEncoder.encode(clave));
            }
            Usuario guardado = usuarioRepository.save(usuario);
            if (guardado.getRol() == Usuario.Rol.ADMIN) {
                administradorRepository.findByUsuarioId(guardado.getId()).orElseGet(() -> {
                    Administrador admin = new Administrador();
                    admin.setUsuario(guardado);
                    admin.setCargo(guardado.getId() != null && guardado.getId() == 60 ? "Administrador Principal" : "Administrador");
                    return administradorRepository.save(admin);
                });
            }
            return guardado;
        }
    }

    @Transactional
    public void eliminar(Integer id) {
        if (id == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El ID de usuario no puede ser nulo.");
        }
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No se puede eliminar. Usuario inexistente con ID: " + id));

        if (id == 60 || usuario.esAdminPrincipal() || (usuario.getRol() == Usuario.Rol.ADMIN && "admin@propath.com".equalsIgnoreCase(usuario.getCorreo()))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No está permitido eliminar la cuenta del administrador principal del sistema.");
        }

        administradorRepository.findByUsuarioId(id).ifPresent(administradorRepository::delete);

        estudianteRepository.findByUsuarioId(id).ifPresent(est -> {
            List<Postulacion> postulaciones = postulacionRepository.findByEstudianteId(est.getId());
            if (!postulaciones.isEmpty()) {
                postulacionRepository.deleteAll(postulaciones);
            }
            estudianteRepository.delete(est);
        });

        empresaRepository.findByUsuarioId(id).ifPresent(emp -> {
            List<OfertaPractica> ofertas = ofertaPracticaRepository.findByEmpresaId(emp.getId());
            for (OfertaPractica ofr : ofertas) {
                List<Postulacion> posts = postulacionRepository.findByOfertaId(ofr.getId());
                if (!posts.isEmpty()) {
                    postulacionRepository.deleteAll(posts);
                }
            }
            if (!ofertas.isEmpty()) {
                ofertaPracticaRepository.deleteAll(ofertas);
            }
            empresaRepository.delete(emp);
        });

        usuarioRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Usuario autenticar(String correo, String contrasena) {
        if (correo == null || correo.trim().isEmpty() || contrasena == null || contrasena.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Correo y contraseña son obligatorios.");
        }
        Usuario usuario = usuarioRepository.findByCorreo(correo.trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas."));

        if (!passwordEncoder.matches(contrasena.trim(), usuario.getContrasena())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas.");
        }

        if (usuario.getEstado() != null && usuario.getEstado() != Usuario.Estado.ACTIVO) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "La cuenta de usuario no está activa.");
        }

        return usuario;
    }

    @Transactional
    public Estudiante registrarEstudiante(String nombre, String apellidos, String correo, String contrasena, String universidad, String carrera, Integer ciclo, String telefono) {
        if (correo == null || correo.trim().isEmpty() || contrasena == null || contrasena.trim().isEmpty() || nombre == null || nombre.trim().isEmpty() || apellidos == null || apellidos.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Todos los campos obligatorios deben ser completados.");
        }
        if (usuarioRepository.findByCorreo(correo.trim()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El correo electrónico ya se encuentra registrado.");
        }
        Usuario usuario = new Usuario(nombre.trim(), apellidos.trim(), correo.trim(), passwordEncoder.encode(contrasena.trim()), Usuario.Rol.ESTUDIANTE, Usuario.Estado.ACTIVO);
        usuario = usuarioRepository.save(usuario);

        Estudiante estudiante = new Estudiante();
        estudiante.setUsuario(usuario);
        estudiante.setApellidos(apellidos.trim());
        estudiante.setUniversidad(universidad != null ? universidad.trim() : "");
        estudiante.setCarrera(carrera != null ? carrera.trim() : "");
        estudiante.setCiclo(ciclo != null ? ciclo : 1);
        estudiante.setTelefono(telefono != null ? telefono.trim() : "");
        estudiante.setCv("");
        return estudianteRepository.save(estudiante);
    }

    @Transactional
    public Empresa registrarEmpresa(String nombre, String apellidos, String razonSocial, String ruc, String correo, String contrasena, String telefono, String direccion, String descripcion) {
        if (correo == null || correo.trim().isEmpty() || contrasena == null || contrasena.trim().isEmpty() || razonSocial == null || razonSocial.trim().isEmpty() || ruc == null || ruc.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Todos los campos obligatorios deben ser completados.");
        }
        if (!ruc.trim().matches("\\d{11}")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El RUC debe tener exactamente 11 dígitos numéricos.");
        }
        if (usuarioRepository.findByCorreo(correo.trim()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El correo electrónico ya se encuentra registrado.");
        }
        if (empresaRepository.existsByRuc(ruc.trim())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El RUC ya se encuentra registrado.");
        }
        String nombreFinal = (nombre != null && !nombre.trim().isEmpty()) ? nombre.trim() : razonSocial.trim();
        String apellidosFinal = (apellidos != null && !apellidos.trim().isEmpty()) ? apellidos.trim() : "";
        Usuario usuario = new Usuario(nombreFinal, apellidosFinal, correo.trim(), passwordEncoder.encode(contrasena.trim()), Usuario.Rol.EMPRESA, Usuario.Estado.ACTIVO);
        usuario = usuarioRepository.save(usuario);

        Empresa empresa = new Empresa();
        empresa.setUsuario(usuario);
        empresa.setRazonSocial(razonSocial.trim());
        empresa.setRuc(ruc.trim());
        empresa.setTelefono(telefono != null ? telefono.trim() : "");
        empresa.setDireccion(direccion != null ? direccion.trim() : "");
        empresa.setDescripcion(descripcion != null ? descripcion.trim() : "");
        empresa.setEstado(Empresa.Estado.PENDIENTE);
        return empresaRepository.save(empresa);
    }
}
