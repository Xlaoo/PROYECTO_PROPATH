package com.propath.servicio;

import com.propath.dao.AdminDAO;
import com.propath.dao.DocumentoEstudianteDAO;
import com.propath.dao.EmpresaDAO;
import com.propath.dao.EntrevistaDAO;
import com.propath.dao.EstudianteDAO;
import com.propath.dao.OfertaPracticaDAO;
import com.propath.dao.PostulacionDAO;
import com.propath.dao.UsuarioDAO;
import com.propath.modelo.DocumentoEstudiante;
import com.propath.modelo.Empresa;
import com.propath.modelo.Entrevista;
import com.propath.modelo.Estudiante;
import com.propath.modelo.OfertaPractica;
import com.propath.modelo.Postulacion;
import com.propath.modelo.RespuestaPostulacion;
import com.propath.modelo.Usuario;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

public class BackendCliente {

    public static String generarAuthHeader(String correo, String contrasena) {
        if (correo == null || contrasena == null) return null;
        String credencial = correo + ":" + contrasena;
        return "Basic " + Base64.getEncoder().encodeToString(credencial.getBytes(StandardCharsets.UTF_8));
    }

    public static Usuario autenticar(String correo, String contrasena) {
        if (correo == null || contrasena == null || correo.trim().isEmpty() || contrasena.trim().isEmpty()) {
            return null;
        }
        return new UsuarioDAO().autenticar(correo.trim(), contrasena);
    }

    public static String registrarEstudiante(String nombre, String apellidos, String correo, String contrasena,
            String universidad, String carrera, int ciclo, String telefono) {
        UsuarioDAO usuarioDAO = new UsuarioDAO();
        if (usuarioDAO.obtenerPorCorreo(correo.trim()) != null) {
            return "El correo electrónico ya se encuentra registrado.";
        }

        Estudiante est = new Estudiante();
        est.setNombre(nombre != null ? nombre.trim() : "");
        est.setApellido(apellidos != null ? apellidos.trim() : "");
        est.setUniversidad(universidad != null ? universidad.trim() : "");
        est.setCarrera(carrera != null ? carrera.trim() : "");
        est.setCiclo(ciclo);
        est.setTelefono(telefono != null ? telefono.trim() : "");
        est.setCv("");

        int id = new EstudianteDAO().registrarConUsuario(est, correo.trim(), contrasena);
        return id > 0 ? null : "Error en el registro del estudiante.";
    }

    public static String registrarEmpresa(String nombre, String apellido, String razonSocial, String ruc,
            String correo, String contrasena, String telefono, String direccion, String descripcion) {
        UsuarioDAO usuarioDAO = new UsuarioDAO();
        EmpresaDAO empresaDAO = new EmpresaDAO();

        if (usuarioDAO.obtenerPorCorreo(correo.trim()) != null) {
            return "El correo electrónico ya se encuentra registrado.";
        }
        if (empresaDAO.obtenerPorRuc(ruc.trim()) != null) {
            return "El RUC ya se encuentra registrado por otra empresa.";
        }

        Empresa emp = new Empresa();
        emp.setRazonSocial(razonSocial != null ? razonSocial.trim() : "");
        emp.setRuc(ruc != null ? ruc.trim() : "");
        emp.setTelefono(telefono != null ? telefono.trim() : "");
        emp.setDireccion(direccion != null ? direccion.trim() : "");
        emp.setDescripcion(descripcion != null ? descripcion.trim() : "");
        emp.setEstado("PENDIENTE");

        int id = empresaDAO.registrarConUsuario(emp, correo.trim(), contrasena,
                nombre != null ? nombre.trim() : "", apellido != null ? apellido.trim() : "");
        return id > 0 ? null : "Error en el registro de la empresa.";
    }

    public static List<OfertaPractica> listarOfertasPublicas() {
        return new OfertaPracticaDAO().listarActivas();
    }

    public static Map<String, Long> obtenerEstadisticasPublicas() {
        return new AdminDAO().obtenerEstadisticasPublicas();
    }

    public static List<Estudiante> listarEstudiantes(String authHeader) {
        return listarEstudiantes();
    }

    public static List<Estudiante> listarEstudiantes() {
        return new EstudianteDAO().listarTodos();
    }

    public static List<Empresa> listarEmpresas(String authHeader) {
        return listarEmpresas();
    }

    public static List<Empresa> listarEmpresas() {
        return new EmpresaDAO().listarTodas();
    }

    public static Empresa obtenerEmpresaPorId(int idEmpresa) {
        return new EmpresaDAO().obtenerPorId(idEmpresa);
    }

    public static List<OfertaPractica> listarOfertas(String authHeader) {
        return listarOfertas();
    }

    public static List<OfertaPractica> listarOfertas() {
        return new OfertaPracticaDAO().listarTodas();
    }

    public static List<Postulacion> listarPostulaciones(String authHeader) {
        return listarPostulaciones();
    }

    public static List<Postulacion> listarPostulaciones() {
        return new PostulacionDAO().listarTodas();
    }

    public static List<Usuario> listarUsuarios() {
        return new UsuarioDAO().listarTodos();
    }

    public static boolean actualizarEstadoEmpresa(int id, String nuevoEstado) {
        return new EmpresaDAO().actualizarEstado(id, nuevoEstado);
    }

    public static Map<String, Object> obtenerEstadisticasAdmin() {
        return new AdminDAO().obtenerEstadisticasAdmin();
    }

    public static boolean actualizarEstadoEstudiante(int id, String nuevoEstado) {
        return new EstudianteDAO().actualizarEstado(id, nuevoEstado);
    }

    public static DocumentoEstudiante obtenerCvEstudiante(int idEstudiante) {
        return new DocumentoEstudianteDAO().obtenerCvEstudiante(idEstudiante);
    }

    public static String guardarCvEstudiante(int idEstudiante, String nombreOriginal, byte[] contenidoPdf) {
        return new DocumentoEstudianteDAO().guardarCv(idEstudiante, nombreOriginal, contenidoPdf);
    }

    public static byte[] descargarCvEstudiante(int idEstudiante, boolean inline) {
        return new DocumentoEstudianteDAO().descargarCv(idEstudiante);
    }

    public static boolean eliminarCvEstudiante(int idEstudiante) {
        return new DocumentoEstudianteDAO().eliminarCv(idEstudiante);
    }

    public static boolean actualizarEstadoOferta(int id, String nuevoEstado) {
        return new OfertaPracticaDAO().actualizarEstado(id, nuevoEstado);
    }

    public static boolean actualizarEstadoPostulacion(int id, String nuevoEstado) {
        return new PostulacionDAO().actualizarEstado(id, nuevoEstado);
    }

    public static boolean actualizarEstadoPostulacion(int id, String nuevoEstado, String motivo, Integer puntuacion) {
        return new PostulacionDAO().evaluarPostulacion(id, nuevoEstado, motivo, puntuacion);
    }

    public static boolean eliminarRegistro(String recurso, int id) {
        return eliminarRegistroConDetalle(recurso, id) == null;
    }

    public static String eliminarRegistroConDetalle(String recurso, int id) {
        if (recurso == null || id <= 0) return "Parámetros inválidos.";
        boolean ok = false;
        if ("usuario".equalsIgnoreCase(recurso) || "usuarios".equalsIgnoreCase(recurso)) {
            ok = new UsuarioDAO().eliminar(id);
        } else if ("empresa".equalsIgnoreCase(recurso) || "empresas".equalsIgnoreCase(recurso)) {
            ok = new EmpresaDAO().eliminar(id);
        } else if ("estudiante".equalsIgnoreCase(recurso) || "estudiantes".equalsIgnoreCase(recurso)) {
            ok = new EstudianteDAO().eliminar(id);
        } else if ("oferta".equalsIgnoreCase(recurso) || "ofertas".equalsIgnoreCase(recurso)) {
            ok = new OfertaPracticaDAO().eliminar(id);
        } else if ("postulacion".equalsIgnoreCase(recurso) || "postulaciones".equalsIgnoreCase(recurso)) {
            ok = new PostulacionDAO().eliminar(id);
        } else if ("entrevista".equalsIgnoreCase(recurso) || "entrevistas".equalsIgnoreCase(recurso)) {
            ok = new EntrevistaDAO().eliminar(id);
        }
        return ok ? null : "No se pudo eliminar el registro seleccionado.";
    }

    public static String activarCuentaEmpresa(int idEmpresa) {
        Empresa emp = new EmpresaDAO().obtenerPorId(idEmpresa);
        if (emp == null) return "Empresa no encontrada.";
        new EmpresaDAO().actualizarEstado(idEmpresa, "APROBADA");
        new UsuarioDAO().actualizarEstado(emp.getIdUsuario(), "ACTIVO");
        return null;
    }

    public static String obtenerDetallePostulacion(int idPostulacion) {
        Postulacion p = new PostulacionDAO().obtenerPorId(idPostulacion);
        if (p == null) return null;
        return String.format(
            "{\"id\":%d,\"estado\":\"%s\",\"fechaPostulacion\":\"%s\",\"motivoRechazo\":\"%s\",\"estudiante\":{\"id\":%d,\"nombre\":\"%s\",\"apellidos\":\"%s\",\"correo\":\"%s\"},\"oferta\":{\"id\":%d,\"titulo\":\"%s\",\"empresa\":{\"id\":%d,\"razonSocial\":\"%s\"}}}",
            p.getId(),
            p.getEstado() != null ? p.getEstado() : "",
            p.getFechaPostulacion() != null ? p.getFechaPostulacion() : "",
            p.getMotivoRechazo() != null ? p.getMotivoRechazo() : "",
            p.getIdEstudiante(),
            p.getEstudiante() != null ? escaparJson(p.getEstudiante()) : "",
            "",
            "",
            p.getIdOferta(),
            p.getOferta() != null ? escaparJson(p.getOferta()) : "",
            0,
            p.getEmpresa() != null ? escaparJson(p.getEmpresa()) : ""
        );
    }

    public static String obtenerRespuestasPostulacion(int idPostulacion, int idEstudiante) {
        List<RespuestaPostulacion> lista = new PostulacionDAO().obtenerRespuestas(idPostulacion);
        if (lista == null || lista.isEmpty()) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < lista.size(); i++) {
            RespuestaPostulacion r = lista.get(i);
            if (i > 0) sb.append(",");
            sb.append("{\"numeroPregunta\":").append(r.getNumeroPregunta())
              .append(",\"pregunta\":\"").append(escaparJson(r.getPregunta()))
              .append("\",\"respuesta\":\"").append(escaparJson(r.getRespuesta()))
              .append("\"}");
        }
        sb.append("]");
        return sb.toString();
    }

    public static String postular(int arg1, int arg2, String[] respuestas) {
        int idEstudiante = arg1;
        int idOferta = arg2;
        if (new OfertaPracticaDAO().obtenerPorId(arg1) != null && new EstudianteDAO().obtenerPorId(arg2) != null) {
            idOferta = arg1;
            idEstudiante = arg2;
        }
        PostulacionDAO dao = new PostulacionDAO();
        if (dao.existePostulacion(idEstudiante, idOferta)) {
            return "Ya te has postulado a esta oferta de prácticas.";
        }
        Postulacion post = new Postulacion();
        post.setIdEstudiante(idEstudiante);
        post.setIdOferta(idOferta);
        post.setEstado("POSTULADO");
        int idPost = dao.crearConRespuestas(post, respuestas);
        return idPost > 0 ? null : "Error al registrar la postulación.";
    }

    public static String actualizarRespuestasPostulacion(int idPostulacion, int idEstudiante, String[] respuestas) {
        boolean ok = new PostulacionDAO().guardarRespuestas(idPostulacion, respuestas);
        return ok ? null : "Error al actualizar las respuestas de la postulación.";
    }

    public static String publicarOferta(int idEmpresa, String titulo, String descripcion, String requisitos,
            String carrera, String modalidad, int vacantes, String fechaLimite, String ubicacion, String remuneracion) {
        OfertaPractica o = new OfertaPractica();
        o.setIdEmpresa(idEmpresa);
        o.setTitulo(titulo);
        o.setDescripcion(descripcion);
        o.setRequisitos(requisitos);
        o.setCarrera(carrera);
        o.setModalidad(modalidad);
        o.setVacantes(vacantes);
        o.setFechaLimite(fechaLimite);
        o.setUbicacion(ubicacion);
        o.setRemuneracion(remuneracion);
        o.setEstado("ACTIVA");
        int id = new OfertaPracticaDAO().crear(o);
        return id > 0 ? null : "Error al publicar la oferta de prácticas.";
    }

    public static String crearUsuario(String nombre, String correo, String contrasena, String rol, String estado) {
        Usuario u = new Usuario();
        u.setNombre(nombre);
        u.setApellidos("");
        u.setCorreo(correo);
        u.setContrasena(contrasena);
        u.setRol(rol);
        u.setEstado(estado);
        int id = new UsuarioDAO().crear(u);
        return id > 0 ? null : "Error al crear el usuario.";
    }

    public static String crearUsuario(String nombre, String apellidos, String correo, String contrasena, String rol,
            String estado) {
        Usuario u = new Usuario();
        u.setNombre(nombre);
        u.setApellidos(apellidos);
        u.setCorreo(correo);
        u.setContrasena(contrasena);
        u.setRol(rol);
        u.setEstado(estado);
        int id = new UsuarioDAO().crear(u);
        return id > 0 ? null : "Error al crear el usuario.";
    }

    public static String actualizarUsuario(int id, String nombre, String apellidos, String correo, String contrasena,
            String estado) {
        UsuarioDAO dao = new UsuarioDAO();
        Usuario u = dao.obtenerPorId(id);
        if (u == null) return "Usuario no encontrado.";
        u.setNombre(nombre);
        u.setApellidos(apellidos);
        u.setCorreo(correo);
        if (contrasena != null && !contrasena.trim().isEmpty()) {
            u.setContrasena(contrasena.trim());
        }
        u.setEstado(estado);
        boolean ok = dao.actualizar(u);
        return ok ? null : "Error al actualizar el usuario.";
    }

    public static String crearEmpresaAdmin(String nombre, String razonSocial, String ruc, String correo,
            String contrasena, String telefono, String direccion, String descripcion, String estado) {
        Empresa emp = new Empresa();
        emp.setRazonSocial(razonSocial);
        emp.setRuc(ruc);
        emp.setTelefono(telefono);
        emp.setDireccion(direccion);
        emp.setDescripcion(descripcion);
        emp.setEstado(estado != null && !estado.trim().isEmpty() ? estado.trim().toUpperCase() : "APROBADA");
        int id = new EmpresaDAO().registrarConUsuario(emp, correo, contrasena, nombre != null ? nombre.trim() : "", "");
        return id > 0 ? null : "Error al registrar la empresa.";
    }

    public static String actualizarEmpresa(int id, String razonSocial, String ruc, String telefono, String estado) {
        EmpresaDAO dao = new EmpresaDAO();
        Empresa emp = dao.obtenerPorId(id);
        if (emp == null) return "Empresa no encontrada.";
        emp.setRazonSocial(razonSocial);
        emp.setRuc(ruc);
        emp.setTelefono(telefono);
        if (estado != null) emp.setEstado(estado);
        boolean ok = dao.actualizar(emp);
        if (estado != null) dao.actualizarEstado(id, estado);
        return ok ? null : "Error al actualizar la empresa.";
    }

    public static String actualizarEmpresa(int id, String razonSocial, String ruc, String telefono, String direccion,
            String descripcion) {
        EmpresaDAO dao = new EmpresaDAO();
        Empresa emp = dao.obtenerPorId(id);
        if (emp == null) return "Empresa no encontrada.";
        emp.setRazonSocial(razonSocial);
        emp.setRuc(ruc);
        emp.setTelefono(telefono);
        emp.setDireccion(direccion);
        emp.setDescripcion(descripcion);
        boolean ok = dao.actualizar(emp);
        return ok ? null : "Error al actualizar la empresa.";
    }

    public static String actualizarEmpresa(int id, String razonSocial, String ruc, String telefono, String direccion,
            String descripcion, String estado, String repNombre, String repApellidos, String correo,
            String contrasena) {
        EmpresaDAO dao = new EmpresaDAO();
        Empresa emp = dao.obtenerPorId(id);
        if (emp == null) return "Empresa no encontrada.";
        emp.setRazonSocial(razonSocial);
        emp.setRuc(ruc);
        emp.setTelefono(telefono);
        emp.setDireccion(direccion);
        emp.setDescripcion(descripcion);
        if (estado != null) emp.setEstado(estado);
        boolean ok = dao.actualizar(emp);
        if (estado != null) dao.actualizarEstado(id, estado);
        if (emp.getIdUsuario() > 0) {
            UsuarioDAO udao = new UsuarioDAO();
            Usuario u = udao.obtenerPorId(emp.getIdUsuario());
            if (u != null) {
                u.setNombre(repNombre);
                u.setApellidos(repApellidos);
                u.setCorreo(correo);
                if (contrasena != null && !contrasena.trim().isEmpty()) {
                    u.setContrasena(contrasena.trim());
                }
                udao.actualizar(u);
            }
        }
        return ok ? null : "Error al actualizar la empresa.";
    }

    public static String actualizarOferta(int id, int idEmpresa, String titulo, String descripcion, String requisitos,
            String carrera, String modalidad, int vacantes, String fechaLimite, String ubicacion,
            String remuneracion, String estado) {
        OfertaPracticaDAO dao = new OfertaPracticaDAO();
        OfertaPractica o = dao.obtenerPorId(id);
        if (o == null) return "Oferta no encontrada.";
        if (o.getIdEmpresa() != idEmpresa) return "La oferta no pertenece a la empresa.";
        o.setTitulo(titulo);
        o.setDescripcion(descripcion);
        o.setRequisitos(requisitos);
        o.setCarrera(carrera);
        o.setModalidad(modalidad);
        o.setVacantes(vacantes);
        o.setFechaLimite(fechaLimite);
        o.setUbicacion(ubicacion);
        o.setRemuneracion(remuneracion);
        if (estado != null) o.setEstado(estado);
        boolean ok = dao.actualizar(o);
        return ok ? null : "Error al actualizar la oferta.";
    }

    public static String actualizarPerfilEstudiante(int idEstudiante, String nombre, String apellidos,
            String universidad, String carrera, int ciclo, String telefono) {
        EstudianteDAO dao = new EstudianteDAO();
        Estudiante est = dao.obtenerPorId(idEstudiante);
        if (est == null) return "Estudiante no encontrado.";
        est.setNombre(nombre);
        est.setApellido(apellidos);
        est.setUniversidad(universidad);
        est.setCarrera(carrera);
        est.setCiclo(ciclo);
        est.setTelefono(telefono);
        boolean ok = dao.actualizar(est);
        return ok ? null : "Error al actualizar el perfil del estudiante.";
    }

    public static Estudiante obtenerEstudiantePorId(int idEstudiante) {
        return new EstudianteDAO().obtenerPorId(idEstudiante);
    }

    public static String consultarEntrevistasEstudiante(int idEstudiante) {
        List<Entrevista> lista = new EntrevistaDAO().listarPorEstudiante(idEstudiante);
        return serializarEntrevistasJson(lista);
    }

    public static String consultarEntrevistas(int idEmpresa) {
        List<Entrevista> lista = new EntrevistaDAO().listarPorEmpresa(idEmpresa);
        return serializarEntrevistasJson(lista);
    }

    public static String guardarEntrevista(Integer idEntrevista, int idPostulacion, String fecha, String hora,
            String modalidad, String enlaceLugar, String estado, String observaciones) {
        EntrevistaDAO dao = new EntrevistaDAO();
        if (idEntrevista != null && idEntrevista > 0) {
            Entrevista e = dao.obtenerPorId(idEntrevista);
            if (e == null) return "Entrevista no encontrada.";
            e.setFechaEntrevista(fecha);
            e.setHoraEntrevista(hora);
            e.setModalidad(modalidad);
            e.setLugarEnlace(enlaceLugar);
            e.setEstado(estado);
            e.setObservaciones(observaciones);
            boolean ok = dao.actualizar(e);
            return ok ? null : "Error al actualizar la entrevista.";
        } else {
            Entrevista e = new Entrevista();
            e.setIdPostulacion(idPostulacion);
            e.setFechaEntrevista(fecha);
            e.setHoraEntrevista(hora);
            e.setModalidad(modalidad);
            e.setLugarEnlace(enlaceLugar);
            e.setEstado(estado);
            e.setObservaciones(observaciones);
            int idGen = dao.crear(e);
            return idGen > 0 ? null : "Error al programar la entrevista.";
        }
    }

    public static String eliminarEntrevista(int idEntrevista) {
        boolean ok = new EntrevistaDAO().eliminar(idEntrevista);
        return ok ? null : "Error al eliminar la entrevista.";
    }

    private static String serializarEntrevistasJson(List<Entrevista> lista) {
        if (lista == null || lista.isEmpty()) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < lista.size(); i++) {
            Entrevista e = lista.get(i);
            if (i > 0) sb.append(",");
            sb.append("{")
              .append("\"id\":").append(e.getId()).append(",")
              .append("\"idPostulacion\":").append(e.getIdPostulacion()).append(",")
              .append("\"idEmpresa\":").append(e.getIdEmpresa()).append(",")
              .append("\"idEstudiante\":").append(e.getIdEstudiante()).append(",")
              .append("\"estudiante\":\"").append(escaparJson(e.getEstudiante())).append("\",")
              .append("\"oferta\":\"").append(escaparJson(e.getOferta())).append("\",")
              .append("\"empresa\":\"").append(escaparJson(e.getEmpresa())).append("\",")
              .append("\"fecha\":\"").append(escaparJson(e.getFechaEntrevista())).append("\",")
              .append("\"hora\":\"").append(escaparJson(e.getHoraEntrevista())).append("\",")
              .append("\"modalidad\":\"").append(escaparJson(e.getModalidad())).append("\",")
              .append("\"enlaceLugar\":\"").append(escaparJson(e.getLugarEnlace())).append("\",")
              .append("\"estado\":\"").append(escaparJson(e.getEstado())).append("\",")
              .append("\"observaciones\":\"").append(escaparJson(e.getObservaciones())).append("\"")
              .append("}");
        }
        sb.append("]");
        return sb.toString();
    }

    private static String escaparJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }
}
