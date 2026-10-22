package com.propath;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.propath.entity.Empresa;
import com.propath.entity.Estudiante;
import com.propath.entity.OfertaPractica;
import com.propath.entity.Postulacion;
import com.propath.entity.Usuario;
import com.propath.repository.EmpresaRepository;
import com.propath.repository.EstudianteRepository;
import com.propath.repository.OfertaPracticaRepository;
import com.propath.repository.PostulacionRepository;
import com.propath.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Base64;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Transactional
@Rollback(true)
class PropathBackendApplicationTests {

    private static final Logger log = LoggerFactory.getLogger(PropathBackendApplicationTests.class);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EstudianteRepository estudianteRepository;

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private OfertaPracticaRepository ofertaPracticaRepository;

    @Autowired
    private PostulacionRepository postulacionRepository;

    private String authAdminHeader;

    @BeforeEach
    void setUpAdminTest() {
        if (usuarioRepository.findByCorreo("admin.auth@propath.com").isEmpty()) {
            Usuario admin = new Usuario("Admin Test", "admin.auth@propath.com",
                    new BCryptPasswordEncoder().encode("AdminAuth123*"),
                    Usuario.Rol.ADMIN, Usuario.Estado.ACTIVO);
            usuarioRepository.save(admin);
        }
        String cred = "admin.auth@propath.com:AdminAuth123*";
        authAdminHeader = "Basic " + Base64.getEncoder().encodeToString(cred.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    @Order(1)
    void contextLoads() {
        log.info("[TEST-1] Verificando inicio correcto del contexto Spring Boot y conexión MariaDB...");
        assertNotNull(mockMvc);
        assertNotNull(usuarioRepository);
        assertNotNull(estudianteRepository);
        assertNotNull(empresaRepository);
        assertNotNull(ofertaPracticaRepository);
        assertNotNull(postulacionRepository);
        log.info("[TEST-1] Contexto y repositorios inicializados correctamente.");
    }

    @Test
    @Order(2)
    void pruebaCrudUsuarios() throws Exception {
        log.info("----------------------------------------------------------------------");
        log.info("[TEST-CRUD-USUARIOS] Probando operaciones CRUD de Usuarios...");

        
        Map<String, Object> nuevoUsuario = Map.of(
                "nombre", "Administrador Prueba",
                "correo", "admin.prueba@propath.com",
                "contrasena", "PasswordSegura123*",
                "rol", "ADMIN",
                "estado", "ACTIVO"
        );

        MvcResult resultadoPost = mockMvc.perform(post("/api/usuarios")
                        .header("Authorization", authAdminHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nuevoUsuario)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.nombre").value("Administrador Prueba"))
                .andExpect(jsonPath("$.correo").value("admin.prueba@propath.com"))
                .andExpect(jsonPath("$.contrasena").doesNotExist())
                .andReturn();

        JsonNode jsonRespuesta = objectMapper.readTree(resultadoPost.getResponse().getContentAsString());
        int idUsuario = jsonRespuesta.get("id").asInt();

        Usuario usuarioGuardado = usuarioRepository.findById(idUsuario).orElseThrow();
        assertNotNull(usuarioGuardado.getContrasena());
        assertTrue(usuarioGuardado.getContrasena().startsWith("$2a$") || usuarioGuardado.getContrasena().startsWith("$2b$"),
                "La contraseña en base de datos debe estar hasheada con BCrypt.");
        log.info("[TEST-CRUD-USUARIOS] POST exitoso (ID: {}). Contraseña cifrada con BCrypt correctamente.", idUsuario);

        
        mockMvc.perform(get("/api/usuarios")
                        .header("Authorization", authAdminHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
        log.info("[TEST-CRUD-USUARIOS] GET /api/usuarios -> 200 OK");

        
        mockMvc.perform(get("/api/usuarios/" + idUsuario)
                        .header("Authorization", authAdminHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correo").value("admin.prueba@propath.com"))
                .andExpect(jsonPath("$.contrasena").doesNotExist());
        log.info("[TEST-CRUD-USUARIOS] GET /api/usuarios/{} -> 200 OK", idUsuario);

        
        Map<String, Object> usuarioActualizado = Map.of(
                "nombre", "Administrador Modificado",
                "correo", "admin.modificado@propath.com",
                "rol", "ADMIN",
                "estado", "ACTIVO"
        );

        mockMvc.perform(put("/api/usuarios/" + idUsuario)
                        .header("Authorization", authAdminHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usuarioActualizado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Administrador Modificado"))
                .andExpect(jsonPath("$.correo").value("admin.modificado@propath.com"));
        log.info("[TEST-CRUD-USUARIOS] PUT /api/usuarios/{} -> 200 OK (Nombre actualizado)", idUsuario);

        
        mockMvc.perform(delete("/api/usuarios/" + idUsuario)
                        .header("Authorization", authAdminHeader))
                .andExpect(status().isNoContent());
        log.info("[TEST-CRUD-USUARIOS] DELETE /api/usuarios/{} -> 204 No Content", idUsuario);

        
        mockMvc.perform(get("/api/usuarios/" + idUsuario)
                        .header("Authorization", authAdminHeader))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
        log.info("[TEST-CRUD-USUARIOS] Verificación posterior: 404 NOT FOUND confirmado.");
        log.info("----------------------------------------------------------------------");
    }

    @Test
    @Order(3)
    void pruebaCrudEstudiantes() throws Exception {
        log.info("----------------------------------------------------------------------");
        log.info("[TEST-CRUD-ESTUDIANTES] Probando operaciones CRUD de Estudiantes...");

        Usuario usuarioEstudiante = new Usuario("Juan", "juan.estudiante@propath.com", "ClaveEst123", Usuario.Rol.ESTUDIANTE, Usuario.Estado.ACTIVO);
        usuarioEstudiante = usuarioRepository.save(usuarioEstudiante);

        Map<String, Object> nuevoEstudiante = Map.of(
                "usuario", Map.of("id", usuarioEstudiante.getId()),
                "apellidos", "Pérez Gómez",
                "universidad", "Universidad Nacional de Ingeniería",
                "carrera", "Ingeniería de Sistemas",
                "ciclo", 7,
                "telefono", "987654321"
        );

        MvcResult resultadoPost = mockMvc.perform(post("/api/estudiantes")
                        .header("Authorization", authAdminHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nuevoEstudiante)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.apellidos").value("Pérez Gómez"))
                .andExpect(jsonPath("$.carrera").value("Ingeniería de Sistemas"))
                .andReturn();

        JsonNode jsonRespuesta = objectMapper.readTree(resultadoPost.getResponse().getContentAsString());
        int idEstudiante = jsonRespuesta.get("id").asInt();
        log.info("[TEST-CRUD-ESTUDIANTES] POST exitoso (ID: {}).", idEstudiante);

        mockMvc.perform(get("/api/estudiantes")
                        .header("Authorization", authAdminHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
        log.info("[TEST-CRUD-ESTUDIANTES] GET /api/estudiantes -> 200 OK");

        mockMvc.perform(get("/api/estudiantes/" + idEstudiante)
                        .header("Authorization", authAdminHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.apellidos").value("Pérez Gómez"));
        log.info("[TEST-CRUD-ESTUDIANTES] GET /api/estudiantes/{} -> 200 OK", idEstudiante);

        Map<String, Object> estudianteActualizado = Map.of(
                "usuario", Map.of("id", usuarioEstudiante.getId()),
                "apellidos", "Pérez Gómez",
                "universidad", "Universidad Nacional de Ingeniería",
                "carrera", "Ciencia de la Computación",
                "ciclo", 8,
                "telefono", "987654321"
        );

        mockMvc.perform(put("/api/estudiantes/" + idEstudiante)
                        .header("Authorization", authAdminHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(estudianteActualizado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.carrera").value("Ciencia de la Computación"))
                .andExpect(jsonPath("$.ciclo").value(8));
        log.info("[TEST-CRUD-ESTUDIANTES] PUT /api/estudiantes/{} -> 200 OK (Carrera y ciclo actualizados)", idEstudiante);

        mockMvc.perform(delete("/api/estudiantes/" + idEstudiante)
                        .header("Authorization", authAdminHeader))
                .andExpect(status().isNoContent());
        log.info("[TEST-CRUD-ESTUDIANTES] DELETE /api/estudiantes/{} -> 204 No Content", idEstudiante);

        mockMvc.perform(get("/api/estudiantes/" + idEstudiante)
                        .header("Authorization", authAdminHeader))
                .andExpect(status().isNotFound());
        log.info("[TEST-CRUD-ESTUDIANTES] Verificación posterior: 404 NOT FOUND confirmado.");
        log.info("----------------------------------------------------------------------");
    }

    @Test
    @Order(4)
    void pruebaCrudEmpresas() throws Exception {
        log.info("----------------------------------------------------------------------");
        log.info("[TEST-CRUD-EMPRESAS] Probando operaciones CRUD de Empresas...");

        Usuario usuarioEmpresa = new Usuario("Tech Solutions Contacto", "contacto@techsolutions.com", "ClaveEmpresa123", Usuario.Rol.EMPRESA, Usuario.Estado.ACTIVO);
        usuarioEmpresa = usuarioRepository.save(usuarioEmpresa);

        Map<String, Object> nuevaEmpresa = Map.of(
                "usuario", Map.of("id", usuarioEmpresa.getId()),
                "razonSocial", "Tech Solutions S.A.C.",
                "ruc", "20123456789",
                "telefono", "014567890",
                "direccion", "Av. Javier Prado Este 1234, San Isidro",
                "estado", "APROBADA"
        );

        MvcResult resultadoPost = mockMvc.perform(post("/api/empresas")
                        .header("Authorization", authAdminHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nuevaEmpresa)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.ruc").value("20123456789"))
                .andExpect(jsonPath("$.razonSocial").value("Tech Solutions S.A.C."))
                .andReturn();

        JsonNode jsonRespuesta = objectMapper.readTree(resultadoPost.getResponse().getContentAsString());
        int idEmpresa = jsonRespuesta.get("id").asInt();
        log.info("[TEST-CRUD-EMPRESAS] POST exitoso (ID: {}).", idEmpresa);

        mockMvc.perform(get("/api/empresas")
                        .header("Authorization", authAdminHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
        log.info("[TEST-CRUD-EMPRESAS] GET /api/empresas -> 200 OK");

        mockMvc.perform(get("/api/empresas/" + idEmpresa)
                        .header("Authorization", authAdminHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ruc").value("20123456789"));
        log.info("[TEST-CRUD-EMPRESAS] GET /api/empresas/{} -> 200 OK", idEmpresa);

        Map<String, Object> empresaActualizada = Map.of(
                "usuario", Map.of(
                        "id", usuarioEmpresa.getId(),
                        "nombre", "Carlos",
                        "apellidos", "Mendoza",
                        "correo", "carlos.mendoza@techsolutions.com"
                ),
                "razonSocial", "Tech Solutions S.A.C.",
                "ruc", "20123456789",
                "telefono", "014567890",
                "direccion", "Av. República de Panamá 4567, Surquillo",
                "descripcion", "Soluciones tecnológicas avanzadas",
                "estado", "ACTIVO"
        );

        mockMvc.perform(put("/api/empresas/" + idEmpresa)
                        .header("Authorization", authAdminHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(empresaActualizada)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.direccion").value("Av. República de Panamá 4567, Surquillo"))
                .andExpect(jsonPath("$.estado").value("APROBADA"))
                .andExpect(jsonPath("$.usuario.nombre").value("Carlos"))
                .andExpect(jsonPath("$.usuario.apellidos").value("Mendoza"))
                .andExpect(jsonPath("$.usuario.correo").value("carlos.mendoza@techsolutions.com"));
        log.info("[TEST-CRUD-EMPRESAS] PUT /api/empresas/{} con estado=ACTIVO -> 200 OK sin error de deserializacion, representante y empresa actualizados.", idEmpresa);

        mockMvc.perform(put("/api/empresas/" + idEmpresa + "/estado")
                        .header("Authorization", authAdminHeader)
                        .param("nuevoEstado", "SUSPENDIDA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("SUSPENDIDA"));
        log.info("[TEST-CRUD-EMPRESAS] PUT /api/empresas/{}/estado -> 200 OK (Estado SUSPENDIDA)", idEmpresa);

        mockMvc.perform(delete("/api/empresas/" + idEmpresa)
                        .header("Authorization", authAdminHeader))
                .andExpect(status().isForbidden());
        log.info("[TEST-CRUD-EMPRESAS] DELETE empresa ACTIVA -> 403 FORBIDDEN rechazado correctamente.");

        mockMvc.perform(put("/api/empresas/" + idEmpresa + "/estado-cuenta")
                        .header("Authorization", authAdminHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\":\"INACTIVO\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuario.estado").value("INACTIVO"));
        log.info("[TEST-CRUD-EMPRESAS] PUT /api/empresas/{}/estado-cuenta INACTIVO -> 200 OK", idEmpresa);

        mockMvc.perform(put("/api/empresas/" + idEmpresa + "/estado-cuenta")
                        .header("Authorization", authAdminHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\":\"ACTIVO\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuario.estado").value("ACTIVO"));
        log.info("[TEST-CRUD-EMPRESAS] PUT /api/empresas/{}/estado-cuenta ACTIVO -> 200 OK", idEmpresa);

        mockMvc.perform(put("/api/empresas/" + idEmpresa + "/estado-cuenta")
                        .header("Authorization", authAdminHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\":\"INACTIVO\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/empresas/" + idEmpresa)
                        .header("Authorization", authAdminHeader))
                .andExpect(status().isNoContent());
        log.info("[TEST-CRUD-EMPRESAS] DELETE empresa INACTIVA -> 204 No Content", idEmpresa);

        mockMvc.perform(get("/api/empresas/" + idEmpresa)
                        .header("Authorization", authAdminHeader))
                .andExpect(status().isNotFound());
        log.info("[TEST-CRUD-EMPRESAS] Verificación posterior: 404 NOT FOUND confirmado.");
        log.info("----------------------------------------------------------------------");
    }

    @Test
    @Order(5)
    void pruebaCrudOfertasPracticas() throws Exception {
        log.info("----------------------------------------------------------------------");
        log.info("[TEST-CRUD-OFERTAS] Probando operaciones CRUD de Ofertas de Prácticas...");

        Usuario usuarioEmpresa = new Usuario("Innovate RRHH", "rrhh@innovate.pe", "ClaveInnovate123", Usuario.Rol.EMPRESA, Usuario.Estado.ACTIVO);
        usuarioEmpresa = usuarioRepository.save(usuarioEmpresa);

        Empresa empresa = new Empresa();
        empresa.setUsuario(usuarioEmpresa);
        empresa.setRazonSocial("Innovate Perú S.A.C.");
        empresa.setRuc("20987654321");
        empresa.setEstado(Empresa.Estado.APROBADA);
        empresa = empresaRepository.save(empresa);

        Map<String, Object> nuevaOferta = Map.of(
                "empresa", Map.of("id", empresa.getId()),
                "titulo", "Practicante Backend Java",
                "descripcion", "Desarrollo de microservicios con Spring Boot y JPA.",
                "requisitos", "Conocimientos de Java 21, Spring Boot y SQL.",
                "carrera", "Ingeniería de Sistemas",
                "modalidad", "HIBRIDA",
                "vacantes", 2,
                "fechaLimite", LocalDate.now().plusMonths(1).toString(),
                "estado", "ACTIVA"
        );

        MvcResult resultadoPost = mockMvc.perform(post("/api/ofertas")
                        .header("Authorization", authAdminHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nuevaOferta)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.titulo").value("Practicante Backend Java"))
                .andExpect(jsonPath("$.modalidad").value("HIBRIDA"))
                .andReturn();

        JsonNode jsonRespuesta = objectMapper.readTree(resultadoPost.getResponse().getContentAsString());
        int idOferta = jsonRespuesta.get("id").asInt();
        log.info("[TEST-CRUD-OFERTAS] POST exitoso (ID: {}).", idOferta);

        mockMvc.perform(get("/api/ofertas")
                        .header("Authorization", authAdminHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
        log.info("[TEST-CRUD-OFERTAS] GET /api/ofertas -> 200 OK");

        mockMvc.perform(get("/api/ofertas/" + idOferta)
                        .header("Authorization", authAdminHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Practicante Backend Java"));
        log.info("[TEST-CRUD-OFERTAS] GET /api/ofertas/{} -> 200 OK", idOferta);

        Map<String, Object> ofertaActualizada = Map.of(
                "empresa", Map.of("id", empresa.getId()),
                "titulo", "Practicante Backend Java",
                "descripcion", "Desarrollo de microservicios con Spring Boot y JPA.",
                "requisitos", "Conocimientos de Java 21, Spring Boot y SQL.",
                "carrera", "Ingeniería de Sistemas",
                "modalidad", "HIBRIDA",
                "vacantes", 4,
                "fechaLimite", LocalDate.now().plusMonths(1).toString(),
                "estado", "ACTIVA"
        );

        mockMvc.perform(put("/api/ofertas/" + idOferta)
                        .header("Authorization", authAdminHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ofertaActualizada)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vacantes").value(4));
        log.info("[TEST-CRUD-OFERTAS] PUT /api/ofertas/{} -> 200 OK (Vacantes actualizadas a 4)", idOferta);

        mockMvc.perform(delete("/api/ofertas/" + idOferta)
                        .header("Authorization", authAdminHeader))
                .andExpect(status().isNoContent());
        log.info("[TEST-CRUD-OFERTAS] DELETE /api/ofertas/{} -> 204 No Content", idOferta);

        mockMvc.perform(get("/api/ofertas/" + idOferta)
                        .header("Authorization", authAdminHeader))
                .andExpect(status().isNotFound());
        log.info("[TEST-CRUD-OFERTAS] Verificación posterior: 404 NOT FOUND confirmado.");
        log.info("----------------------------------------------------------------------");
    }

    @Test
    @Order(6)
    void pruebaCrudPostulacionesYControlDuplicados() throws Exception {
        log.info("----------------------------------------------------------------------");
        log.info("[TEST-CRUD-POSTULACIONES] Probando operaciones CRUD de Postulaciones y unicidad...");

        Usuario userEst = new Usuario("Carlos", "carlos.postulante@propath.com", "Clave123", Usuario.Rol.ESTUDIANTE, Usuario.Estado.ACTIVO);
        userEst = usuarioRepository.save(userEst);
        Estudiante estudiante = new Estudiante();
        estudiante.setUsuario(userEst);
        estudiante.setApellidos("Sánchez");
        estudiante = estudianteRepository.save(estudiante);

        Usuario userEmp = new Usuario("Global Corp", "rrhh@globalcorp.com", "Clave456", Usuario.Rol.EMPRESA, Usuario.Estado.ACTIVO);
        userEmp = usuarioRepository.save(userEmp);
        Empresa empresa = new Empresa();
        empresa.setUsuario(userEmp);
        empresa.setRazonSocial("Global Corp S.A.");
        empresa.setRuc("20555666777");
        empresa.setEstado(Empresa.Estado.APROBADA);
        empresa = empresaRepository.save(empresa);

        OfertaPractica oferta = new OfertaPractica();
        oferta.setEmpresa(empresa);
        oferta.setTitulo("Practicante Frontend Web");
        oferta.setDescripcion("Desarrollo de interfaces modernas.");
        oferta.setCarrera("Ingeniería de Sistemas");
        oferta.setModalidad(OfertaPractica.Modalidad.VIRTUAL);
        oferta.setVacantes(1);
        oferta.setFechaLimite(LocalDate.now().plusWeeks(2));
        oferta.setEstado(OfertaPractica.Estado.ACTIVA);
        oferta = ofertaPracticaRepository.save(oferta);

        Map<String, Object> nuevaPostulacion = Map.of(
                "estudiante", Map.of("id", estudiante.getId()),
                "oferta", Map.of("id", oferta.getId()),
                "estado", "POSTULADO"
        );

        MvcResult resultadoPost = mockMvc.perform(post("/api/postulaciones")
                        .header("Authorization", authAdminHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nuevaPostulacion)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.estado").value("POSTULADO"))
                .andReturn();

        JsonNode jsonRespuesta = objectMapper.readTree(resultadoPost.getResponse().getContentAsString());
        int idPostulacion = jsonRespuesta.get("id").asInt();
        log.info("[TEST-CRUD-POSTULACIONES] POST inicial exitoso (ID: {}).", idPostulacion);

        mockMvc.perform(post("/api/postulaciones")
                        .header("Authorization", authAdminHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nuevaPostulacion)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").exists());
        log.info("[TEST-CRUD-POSTULACIONES] Postulación duplicada rechazada con éxito (409 CONFLICT).");

        mockMvc.perform(get("/api/postulaciones")
                        .header("Authorization", authAdminHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
        log.info("[TEST-CRUD-POSTULACIONES] GET /api/postulaciones -> 200 OK");

        mockMvc.perform(get("/api/postulaciones/" + idPostulacion)
                        .header("Authorization", authAdminHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(idPostulacion));
        log.info("[TEST-CRUD-POSTULACIONES] GET /api/postulaciones/{} -> 200 OK", idPostulacion);

        Map<String, Object> postulacionActualizada = Map.of(
                "estudiante", Map.of("id", estudiante.getId()),
                "oferta", Map.of("id", oferta.getId()),
                "estado", "EN_REVISION"
        );

        mockMvc.perform(put("/api/postulaciones/" + idPostulacion)
                        .header("Authorization", authAdminHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postulacionActualizada)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("EN_REVISION"));
        log.info("[TEST-CRUD-POSTULACIONES] PUT /api/postulaciones/{} -> 200 OK (Estado cambiado a EN_REVISION)", idPostulacion);

        mockMvc.perform(delete("/api/postulaciones/" + idPostulacion)
                        .header("Authorization", authAdminHeader))
                .andExpect(status().isNoContent());
        log.info("[TEST-CRUD-POSTULACIONES] DELETE /api/postulaciones/{} -> 204 No Content", idPostulacion);

        mockMvc.perform(get("/api/postulaciones/" + idPostulacion)
                        .header("Authorization", authAdminHeader))
                .andExpect(status().isNotFound());
        log.info("[TEST-CRUD-POSTULACIONES] Verificación posterior: 404 NOT FOUND confirmado.");
        log.info("----------------------------------------------------------------------");
    }

    @Test
    @Order(7)
    void pruebaValidacionErroresHttp400Y404() throws Exception {
        log.info("----------------------------------------------------------------------");
        log.info("[TEST-ERRORES-HTTP] Verificando respuestas ante entradas inválidas e IDs inexistentes...");

        Map<String, Object> usuarioInvalido = Map.of(
                "nombre", "",
                "correo", "",
                "rol", "ADMIN"
        );
        mockMvc.perform(post("/api/usuarios")
                        .header("Authorization", authAdminHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usuarioInvalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
        log.info("[TEST-ERRORES-HTTP] POST /api/usuarios con datos vacíos -> 400 BAD REQUEST confirmado.");

        Map<String, Object> empresaRucInvalido = Map.of(
                "razonSocial", "Empresa Falsa",
                "ruc", "123"
        );
        mockMvc.perform(post("/api/empresas")
                        .header("Authorization", authAdminHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(empresaRucInvalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
        log.info("[TEST-ERRORES-HTTP] POST /api/empresas con RUC inválido -> 400 BAD REQUEST confirmado.");

        mockMvc.perform(get("/api/usuarios/999999")
                        .header("Authorization", authAdminHeader))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
        log.info("[TEST-ERRORES-HTTP] GET /api/usuarios/999999 -> 404 NOT FOUND confirmado.");

        log.info("[TEST-ERRORES-HTTP] Pruebas de errores HTTP completadas exitosamente.");
        log.info("----------------------------------------------------------------------");
    }

    @Test
    @Order(8)
    void pruebaSeguridadYAutenticacionAdmin() throws Exception {
        log.info("----------------------------------------------------------------------");
        log.info("[TEST-SEGURIDAD] Verificando protección de endpoints y login real...");

        
        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").exists());
        log.info("[TEST-SEGURIDAD] Petición sin credenciales rechazada correctamente con 401 Unauthorized.");

        
        String credErroneas = "Basic " + Base64.getEncoder().encodeToString("fake@propath.com:wrongpass".getBytes(StandardCharsets.UTF_8));
        mockMvc.perform(get("/api/usuarios")
                        .header("Authorization", credErroneas))
                .andExpect(status().isUnauthorized());
        log.info("[TEST-SEGURIDAD] Petición con credenciales incorrectas rechazada con 401 Unauthorized.");

        
        Map<String, String> loginValido = Map.of(
                "correo", "admin.auth@propath.com",
                "contrasena", "AdminAuth123*"
        );
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginValido)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correo").value("admin.auth@propath.com"))
                .andExpect(jsonPath("$.rol").value("ADMIN"))
                .andExpect(jsonPath("$.contrasena").doesNotExist());
        log.info("[TEST-SEGURIDAD] Login administrativo exitoso en /api/auth/login (200 OK).");

        
        Map<String, String> loginClaveErronea = Map.of(
                "correo", "admin.auth@propath.com",
                "contrasena", "ClaveTotalmenteErronea"
        );
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginClaveErronea)))
                .andExpect(status().isUnauthorized());
        log.info("[TEST-SEGURIDAD] Intento de login con clave incorrecta rechazado con 401 Unauthorized.");

        log.info("[TEST-SEGURIDAD] Pruebas de seguridad completadas exitosamente.");
        log.info("----------------------------------------------------------------------");
    }

    @Test
    @Order(9)
    void pruebaCrudDocumentosEstudiante() throws Exception {
        log.info("----------------------------------------------------------------------");
        log.info("[TEST-CRUD-DOCUMENTOS] Probando operaciones CRUD de Documentos del Estudiante (PDF)...");

        Usuario userEst = new Usuario("Lucia", "lucia.doc@propath.com", "ClaveDoc123", Usuario.Rol.ESTUDIANTE, Usuario.Estado.ACTIVO);
        userEst = usuarioRepository.save(userEst);

        Estudiante estudiante = new Estudiante();
        estudiante.setUsuario(userEst);
        estudiante.setApellidos("Mendoza");
        estudiante.setUniversidad("UNMSM");
        estudiante.setCarrera("Ingeniería de Software");
        estudiante.setCiclo(8);
        estudiante = estudianteRepository.save(estudiante);
        int idEst = estudiante.getId();

        byte[] fakePdfValido = "%PDF-1.4\n1 0 obj\n<< /Type /Catalog >>\nendobj\ntrailer\n<< /Root 1 0 R >>\n%%EOF".getBytes(StandardCharsets.UTF_8);
        org.springframework.mock.web.MockMultipartFile archivoPdf = new org.springframework.mock.web.MockMultipartFile(
                "archivo",
                "Mi_Curriculum_Vitae.pdf",
                "application/pdf",
                fakePdfValido
        );

        mockMvc.perform(multipart("/api/estudiantes/" + idEst + "/documentos/cv")
                        .file(archivoPdf)
                        .header("Authorization", authAdminHeader))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.nombreArchivo").value("Mi_Curriculum_Vitae.pdf"))
                .andExpect(jsonPath("$.tamanioBytes").value(fakePdfValido.length))
                .andExpect(jsonPath("$.tipoDocumento").value("CV"));
        log.info("[TEST-CRUD-DOCUMENTOS] CREATE: CV subido correctamente y persistido (201 Created).");

        Estudiante estActualizado = estudianteRepository.findById(idEst).orElseThrow();
        assertEquals("Mi_Curriculum_Vitae.pdf", estActualizado.getCv(), "El campo CV en la tabla estudiantes debe sincronizarse.");

        mockMvc.perform(get("/api/estudiantes/" + idEst + "/documentos/cv")
                        .header("Authorization", authAdminHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreArchivo").value("Mi_Curriculum_Vitae.pdf"));
        log.info("[TEST-CRUD-DOCUMENTOS] READ: Metadata de CV consultada exitosamente (200 OK).");

        mockMvc.perform(get("/api/estudiantes/" + idEst + "/documentos/cv/descargar")
                        .param("inline", "true")
                        .header("Authorization", authAdminHeader))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"))
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("inline")))
                .andExpect(content().bytes(fakePdfValido));
        log.info("[TEST-CRUD-DOCUMENTOS] READ STREAM: Visualización inline de PDF transmitida correctamente (200 OK).");

        byte[] nuevoPdfValido = "%PDF-1.5\nNuevo contenido actualizado de CV\n%%EOF".getBytes(StandardCharsets.UTF_8);
        org.springframework.mock.web.MockMultipartFile nuevoArchivoPdf = new org.springframework.mock.web.MockMultipartFile(
                "archivo",
                "CV_Actualizado_2026.pdf",
                "application/pdf",
                nuevoPdfValido
        );

        mockMvc.perform(multipart("/api/estudiantes/" + idEst + "/documentos/cv")
                        .file(nuevoArchivoPdf)
                        .with(request -> { request.setMethod("PUT"); return request; })
                        .header("Authorization", authAdminHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreArchivo").value("CV_Actualizado_2026.pdf"))
                .andExpect(jsonPath("$.tamanioBytes").value(nuevoPdfValido.length));
        log.info("[TEST-CRUD-DOCUMENTOS] UPDATE: Reemplazo seguro de CV completado exitosamente (200 OK).");

        byte[] fakeNoPdf = "ESTO NO ES UN PDF".getBytes(StandardCharsets.UTF_8);
        org.springframework.mock.web.MockMultipartFile archivoInvalido = new org.springframework.mock.web.MockMultipartFile(
                "archivo",
                "archivo_falso.pdf",
                "application/pdf",
                fakeNoPdf
        );
        mockMvc.perform(multipart("/api/estudiantes/" + idEst + "/documentos/cv")
                        .file(archivoInvalido)
                        .with(request -> { request.setMethod("PUT"); return request; })
                        .header("Authorization", authAdminHeader))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
        log.info("[TEST-CRUD-DOCUMENTOS] VALIDACIÓN: Archivo no-PDF rechazado correctamente con 400 Bad Request.");

        byte[] archivoGigante = new byte[6 * 1024 * 1024];
        System.arraycopy("%PDF".getBytes(StandardCharsets.UTF_8), 0, archivoGigante, 0, 4);
        org.springframework.mock.web.MockMultipartFile archivoPesado = new org.springframework.mock.web.MockMultipartFile(
                "archivo",
                "archivo_pesado.pdf",
                "application/pdf",
                archivoGigante
        );
        mockMvc.perform(multipart("/api/estudiantes/" + idEst + "/documentos/cv")
                        .file(archivoPesado)
                        .with(request -> { request.setMethod("PUT"); return request; })
                        .header("Authorization", authAdminHeader))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
        log.info("[TEST-CRUD-DOCUMENTOS] VALIDACIÓN: Archivo > 5 MB rechazado correctamente con 400 Bad Request.");

        mockMvc.perform(delete("/api/estudiantes/" + idEst + "/documentos/cv")
                        .header("Authorization", authAdminHeader))
                .andExpect(status().isNoContent());
        log.info("[TEST-CRUD-DOCUMENTOS] DELETE: CV eliminado correctamente (204 No Content).");

        mockMvc.perform(get("/api/estudiantes/" + idEst + "/documentos/cv")
                        .header("Authorization", authAdminHeader))
                .andExpect(status().isNotFound());
        log.info("[TEST-CRUD-DOCUMENTOS] Verificación posterior: 404 NOT FOUND tras eliminación de documento.");
        log.info("----------------------------------------------------------------------");
    }

    @Test
    @Order(10)
    void pruebaCambiarEstadoEstudianteYControlLogin() throws Exception {
        log.info("----------------------------------------------------------------------");
        log.info("[TEST-ACTIVACION-ESTUDIANTE] Probando activación y bloqueo real de cuenta de estudiante...");

        Usuario userEst = new Usuario("Roberto", "roberto.activar@propath.com",
                new BCryptPasswordEncoder().encode("ClaveRoberto123*"),
                Usuario.Rol.ESTUDIANTE, Usuario.Estado.ACTIVO);
        userEst = usuarioRepository.save(userEst);

        Estudiante estudiante = new Estudiante();
        estudiante.setUsuario(userEst);
        estudiante.setApellidos("Vargas");
        estudiante.setUniversidad("PUCP");
        estudiante.setCarrera("Ingeniería Informática");
        estudiante.setCiclo(6);
        estudiante = estudianteRepository.save(estudiante);
        int idEst = estudiante.getId();

        Map<String, String> bodyInactivo = Map.of("estado", "INACTIVO");
        mockMvc.perform(put("/api/estudiantes/" + idEst + "/estado")
                        .header("Authorization", authAdminHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bodyInactivo)))
                .andExpect(status().isOk());

        Usuario userInactivo = usuarioRepository.findById(userEst.getId()).orElseThrow();
        assertEquals(Usuario.Estado.INACTIVO, userInactivo.getEstado(), "El estado en la base de datos debe ser INACTIVO");
        log.info("[TEST-ACTIVACION-ESTUDIANTE] Cuenta de estudiante pasada a INACTIVO en MariaDB.");

        Map<String, String> loginEst = Map.of(
                "correo", "roberto.activar@propath.com",
                "contrasena", "ClaveRoberto123*"
        );
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginEst)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").exists());
        log.info("[TEST-ACTIVACION-ESTUDIANTE] Login de estudiante inactivo rechazado con 403 Forbidden correctamente.");

        Map<String, String> bodyActivo = Map.of("estado", "ACTIVO");
        mockMvc.perform(put("/api/estudiantes/" + idEst + "/estado")
                        .header("Authorization", authAdminHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bodyActivo)))
                .andExpect(status().isOk());

        Usuario userActivo = usuarioRepository.findById(userEst.getId()).orElseThrow();
        assertEquals(Usuario.Estado.ACTIVO, userActivo.getEstado(), "El estado en la base de datos debe ser ACTIVO");
        log.info("[TEST-ACTIVACION-ESTUDIANTE] Check ejecutado: cuenta reactivada a ACTIVO en MariaDB.");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginEst)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correo").value("roberto.activar@propath.com"))
                .andExpect(jsonPath("$.estado").value("ACTIVO"));
        log.info("[TEST-ACTIVACION-ESTUDIANTE] Login de estudiante reactivado exitoso con 200 OK.");
        log.info("----------------------------------------------------------------------");
    }

    @Test
    @Order(11)
    void pruebaEstadisticasAdmin() throws Exception {
        log.info("----------------------------------------------------------------------");
        log.info("[TEST-ESTADISTICAS-ADMIN] Verificando métricas reales de MariaDB para el Dashboard...");

        MvcResult result = mockMvc.perform(get("/api/admin/estadisticas")
                        .header("Authorization", authAdminHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalEstudiantes").isNumber())
                .andExpect(jsonPath("$.estudiantesEstaSemana").isNumber())
                .andExpect(jsonPath("$.totalEmpresas").isNumber())
                .andExpect(jsonPath("$.empresasAprobadas").isNumber())
                .andExpect(jsonPath("$.empresasPendientes").isNumber())
                .andExpect(jsonPath("$.totalOfertas").isNumber())
                .andExpect(jsonPath("$.ofertasActivas").isNumber())
                .andExpect(jsonPath("$.ofertasPendientes").isNumber())
                .andExpect(jsonPath("$.totalPostulaciones").isNumber())
                .andExpect(jsonPath("$.postulacionesEnRevision").isNumber())
                .andExpect(jsonPath("$.postulacionesSeleccionadas").isNumber())
                .andReturn();

        JsonNode stats = objectMapper.readTree(result.getResponse().getContentAsString());
        long realEst = usuarioRepository.countByRol(Usuario.Rol.ESTUDIANTE);
        long realEmp = empresaRepository.count();
        long realAprobadas = empresaRepository.countByEstado(Empresa.Estado.APROBADA);
        long realPendientes = empresaRepository.countByEstado(Empresa.Estado.PENDIENTE);

        assertEquals(realEst, stats.get("totalEstudiantes").asLong());
        assertEquals(realEmp, stats.get("totalEmpresas").asLong());
        assertEquals(realAprobadas, stats.get("empresasAprobadas").asLong());
        assertEquals(realPendientes, stats.get("empresasPendientes").asLong());

        log.info("[TEST-ESTADISTICAS-ADMIN] Estadísticas validadas: Estudiantes={}, Empresas={}, Aprobadas={}, Pendientes={}.",
                realEst, realEmp, realAprobadas, realPendientes);
        log.info("----------------------------------------------------------------------");
    }

    @Test
    @Order(12)
    void pruebaAprobacionEmpresaYBloqueoOperativo() throws Exception {
        log.info("----------------------------------------------------------------------");
        log.info("[TEST-APROBACION-BLOQUEO] Verificando bloqueo de empresas no aprobadas y aprobación...");

        Usuario userEmp = new Usuario("Empresa Test Pendiente", "test.pendiente@empresa.com",
                new BCryptPasswordEncoder().encode("Clave123*"),
                Usuario.Rol.EMPRESA, Usuario.Estado.ACTIVO);
        userEmp = usuarioRepository.save(userEmp);

        Empresa emp = new Empresa();
        emp.setUsuario(userEmp);
        emp.setRazonSocial("Startup Pendiente S.A.C.");
        emp.setRuc("20777888999");
        emp.setEstado(Empresa.Estado.PENDIENTE);
        emp = empresaRepository.save(emp);
        int idEmp = emp.getId();

        Map<String, Object> ofertaData = Map.of(
                "empresa", Map.of("id", idEmp),
                "titulo", "Oferta No Permitida",
                "descripcion", "Debería fallar con 403",
                "carrera", "Ingeniería de Sistemas",
                "modalidad", "VIRTUAL",
                "vacantes", 1,
                "fechaLimite", LocalDate.now().plusWeeks(2).toString(),
                "estado", "ACTIVA"
        );

        mockMvc.perform(post("/api/ofertas")
                        .header("Authorization", authAdminHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ofertaData)))
                .andExpect(status().isForbidden());
        log.info("[TEST-APROBACION-BLOQUEO] Empresa PENDIENTE bloqueada al publicar oferta (403 Forbidden confirmado).");

        mockMvc.perform(put("/api/empresas/" + idEmp + "/estado")
                        .header("Authorization", authAdminHeader)
                        .param("nuevoEstado", "APROBADA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("APROBADA"));

        Empresa empAprobada = empresaRepository.findById(idEmp).orElseThrow();
        assertEquals(Empresa.Estado.APROBADA, empAprobada.getEstado());
        assertEquals(Usuario.Estado.ACTIVO, empAprobada.getUsuario().getEstado(), "El estado de cuenta de usuario no debe modificarse al aprobar.");
        log.info("[TEST-APROBACION-BLOQUEO] Empresa aprobada exitosamente: estado={}, cuenta={}.",
                empAprobada.getEstado(), empAprobada.getUsuario().getEstado());

        mockMvc.perform(post("/api/ofertas")
                        .header("Authorization", authAdminHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ofertaData)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists());
        log.info("[TEST-APROBACION-BLOQUEO] Empresa APROBADA y ACTIVA ahora puede publicar ofertas (201 Created).");

        mockMvc.perform(put("/api/empresas/" + idEmp + "/estado-cuenta")
                        .header("Authorization", authAdminHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\":\"INACTIVO\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/ofertas")
                        .header("Authorization", authAdminHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ofertaData)))
                .andExpect(status().isForbidden());
        log.info("[TEST-APROBACION-BLOQUEO] Empresa APROBADA pero cuenta INACTIVA bloqueada correctamente (403 Forbidden).");
        log.info("----------------------------------------------------------------------");
    }
}
