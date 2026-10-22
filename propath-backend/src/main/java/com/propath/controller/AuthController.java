package com.propath.controller;

import com.propath.entity.Empresa;
import com.propath.entity.Estudiante;
import com.propath.entity.OfertaPractica;
import com.propath.entity.Usuario;
import com.propath.repository.EmpresaRepository;
import com.propath.repository.EstudianteRepository;
import com.propath.repository.OfertaPracticaRepository;
import com.propath.repository.PostulacionRepository;
import com.propath.service.UsuarioService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsuarioService usuarioService;
    private final EstudianteRepository estudianteRepository;
    private final EmpresaRepository empresaRepository;
    private final OfertaPracticaRepository ofertaPracticaRepository;
    private final PostulacionRepository postulacionRepository;

    public AuthController(UsuarioService usuarioService,
                          EstudianteRepository estudianteRepository,
                          EmpresaRepository empresaRepository,
                          OfertaPracticaRepository ofertaPracticaRepository,
                          PostulacionRepository postulacionRepository) {
        this.usuarioService = usuarioService;
        this.estudianteRepository = estudianteRepository;
        this.empresaRepository = empresaRepository;
        this.ofertaPracticaRepository = ofertaPracticaRepository;
        this.postulacionRepository = postulacionRepository;
    }

    public static class LoginRequest {
        @NotBlank(message = "El correo no puede estar vacío.")
        @Email(message = "Formato de correo inválido.")
        private String correo;

        @NotBlank(message = "La contraseña no puede estar vacía.")
        private String contrasena;

        public String getCorreo() {
            return correo;
        }

        public void setCorreo(String correo) {
            this.correo = correo;
        }

        public String getContrasena() {
            return contrasena;
        }

        public void setContrasena(String contrasena) {
            this.contrasena = contrasena;
        }
    }

    public static class RegistroEstudianteRequest {
        private String nombre;
        private String apellidos;
        private String correo;
        private String contrasena;
        private String universidad;
        private String carrera;
        private Integer ciclo;
        private String telefono;

        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        public String getApellidos() { return apellidos; }
        public void setApellidos(String apellidos) { this.apellidos = apellidos; }
        public String getCorreo() { return correo; }
        public void setCorreo(String correo) { this.correo = correo; }
        public String getContrasena() { return contrasena; }
        public void setContrasena(String contrasena) { this.contrasena = contrasena; }
        public String getUniversidad() { return universidad; }
        public void setUniversidad(String universidad) { this.universidad = universidad; }
        public String getCarrera() { return carrera; }
        public void setCarrera(String carrera) { this.carrera = carrera; }
        public Integer getCiclo() { return ciclo; }
        public void setCiclo(Integer ciclo) { this.ciclo = ciclo; }
        public String getTelefono() { return telefono; }
        public void setTelefono(String telefono) { this.telefono = telefono; }
    }

    public static class RegistroEmpresaRequest {
        private String nombre;
        private String apellidos;
        private String razonSocial;
        private String ruc;
        private String correo;
        private String contrasena;
        private String telefono;
        private String direccion;
        private String descripcion;

        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        public String getApellidos() { return apellidos; }
        public void setApellidos(String apellidos) { this.apellidos = apellidos; }
        public String getRazonSocial() { return razonSocial; }
        public void setRazonSocial(String razonSocial) { this.razonSocial = razonSocial; }
        public String getRuc() { return ruc; }
        public void setRuc(String ruc) { this.ruc = ruc; }
        public String getCorreo() { return correo; }
        public void setCorreo(String correo) { this.correo = correo; }
        public String getContrasena() { return contrasena; }
        public void setContrasena(String contrasena) { this.contrasena = contrasena; }
        public String getTelefono() { return telefono; }
        public void setTelefono(String telefono) { this.telefono = telefono; }
        public String getDireccion() { return direccion; }
        public void setDireccion(String direccion) { this.direccion = direccion; }
        public String getDescripcion() { return descripcion; }
        public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            Usuario usuario = usuarioService.autenticar(loginRequest.getCorreo(), loginRequest.getContrasena());

            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("idUsuario", usuario.getId());
            respuesta.put("nombre", usuario.getNombre());
            respuesta.put("apellidos", usuario.getApellidos() != null ? usuario.getApellidos() : "");
            respuesta.put("correo", usuario.getCorreo());
            respuesta.put("rol", usuario.getRol().name());
            respuesta.put("estado", usuario.getEstado().name());

            if (usuario.getRol() == Usuario.Rol.ESTUDIANTE) {
                estudianteRepository.findByUsuarioId(usuario.getId())
                        .ifPresent(est -> respuesta.put("idEstudiante", est.getId()));
            } else if (usuario.getRol() == Usuario.Rol.EMPRESA) {
                empresaRepository.findByUsuarioId(usuario.getId())
                        .ifPresent(emp -> {
                            respuesta.put("idEmpresa", emp.getId());
                            respuesta.put("razonSocial", emp.getRazonSocial());
                            respuesta.put("estadoEmpresa", emp.getEstado().name());
                        });
            }

            return ResponseEntity.ok(respuesta);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        } catch (IllegalStateException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }
    }

    @PostMapping("/register/estudiante")
    public ResponseEntity<?> registrarEstudiante(@RequestBody RegistroEstudianteRequest req) {
        try {
            Estudiante estudiante = usuarioService.registrarEstudiante(
                    req.getNombre(), req.getApellidos(), req.getCorreo(), req.getContrasena(),
                    req.getUniversidad(), req.getCarrera(), req.getCiclo(), req.getTelefono()
            );
            Map<String, Object> res = new HashMap<>();
            res.put("idEstudiante", estudiante.getId());
            res.put("nombre", estudiante.getUsuario().getNombre());
            res.put("apellidos", estudiante.getApellidos());
            res.put("correo", estudiante.getUsuario().getCorreo());
            res.put("rol", estudiante.getUsuario().getRol().name());
            res.put("mensaje", "Registro de estudiante completado exitosamente.");
            return ResponseEntity.status(HttpStatus.CREATED).body(res);
        } catch (IllegalStateException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @PostMapping("/register/empresa")
    public ResponseEntity<?> registrarEmpresa(@RequestBody RegistroEmpresaRequest req) {
        try {
            Empresa empresa = usuarioService.registrarEmpresa(
                    req.getNombre(), req.getApellidos(), req.getRazonSocial(), req.getRuc(), req.getCorreo(),
                    req.getContrasena(), req.getTelefono(), req.getDireccion(), req.getDescripcion()
            );
            Map<String, Object> res = new HashMap<>();
            res.put("idEmpresa", empresa.getId());
            res.put("razonSocial", empresa.getRazonSocial());
            res.put("ruc", empresa.getRuc());
            res.put("correo", empresa.getUsuario().getCorreo());
            res.put("rol", empresa.getUsuario().getRol().name());
            res.put("estado", empresa.getEstado().name());
            res.put("mensaje", "Registro de empresa completado exitosamente.");
            return ResponseEntity.status(HttpStatus.CREATED).body(res);
        } catch (IllegalStateException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @GetMapping("/ofertas-publicas")
    public ResponseEntity<List<OfertaPractica>> listarOfertasPublicas() {
        return ResponseEntity.ok(ofertaPracticaRepository.findByEstado(OfertaPractica.Estado.ACTIVA));
    }

    @GetMapping("/estadisticas-publicas")
    public ResponseEntity<Map<String, Long>> obtenerEstadisticasPublicas() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("estudiantes", estudianteRepository.count());
        stats.put("empresas", empresaRepository.count());
        stats.put("ofertas", ofertaPracticaRepository.count());
        stats.put("postulaciones", postulacionRepository.count());
        return ResponseEntity.ok(stats);
    }
}
