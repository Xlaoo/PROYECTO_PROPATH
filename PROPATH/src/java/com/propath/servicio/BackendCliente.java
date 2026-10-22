package com.propath.servicio;

import com.propath.modelo.DocumentoEstudiante;
import com.propath.modelo.Empresa;
import com.propath.modelo.Estudiante;
import com.propath.modelo.OfertaPractica;
import com.propath.modelo.Postulacion;
import com.propath.modelo.Usuario;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BackendCliente {

    private static final String BASE_URL = System.getProperty("PROPATH_BACKEND_URL", "http://localhost:8081/api");
    private static final String INTERNAL_KEY = "propath-internal-secret-2026";
    private static final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    public static String generarAuthHeader(String correo, String contrasena) {
        if (correo == null || contrasena == null)
            return null;
        String credencial = correo + ":" + contrasena;
        return "Basic " + Base64.getEncoder().encodeToString(credencial.getBytes(StandardCharsets.UTF_8));
    }

    public static Usuario autenticar(String correo, String contrasena) {
        if (correo == null || contrasena == null || correo.trim().isEmpty() || contrasena.trim().isEmpty()) {
            return null;
        }

        try {
            String jsonBody = String.format("{\"correo\":\"%s\",\"contrasena\":\"%s\"}",
                    escaparJson(correo.trim()), escaparJson(contrasena.trim()));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/auth/login"))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(5))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            if (response.statusCode() == 200) {
                String body = response.body();
                int id = extraerEntero(body, "idUsuario");
                String nombre = extraerCadena(body, "nombre");
                String apellidos = extraerCadena(body, "apellidos");
                String email = extraerCadena(body, "correo");
                String rol = extraerCadena(body, "rol");
                String estado = extraerCadena(body, "estado");

                Usuario usuario = new Usuario(id, nombre, apellidos != null ? apellidos : "", email, rol, estado);

                int idEst = extraerEntero(body, "idEstudiante");
                if (idEst > 0) {
                    usuario.setIdEstudiante(idEst);
                }

                int idEmp = extraerEntero(body, "idEmpresa");
                if (idEmp > 0) {
                    usuario.setIdEmpresa(idEmp);
                }

                String razon = extraerCadena(body, "razonSocial");
                if (razon != null && !razon.isEmpty()) {
                    usuario.setRazonSocial(razon);
                }

                return usuario;
            }
        } catch (Exception e) {
            System.err.println("Error comunicando con backend en autenticar: " + e.getMessage());
        }
        return null;
    }

    public static String registrarEstudiante(String nombre, String apellidos, String correo, String contrasena,
            String universidad, String carrera, int ciclo, String telefono) {
        try {
            String jsonBody = String.format(
                    "{\"nombre\":\"%s\",\"apellidos\":\"%s\",\"correo\":\"%s\",\"contrasena\":\"%s\"," +
                            "\"universidad\":\"%s\",\"carrera\":\"%s\",\"ciclo\":%d,\"telefono\":\"%s\"}",
                    escaparJson(nombre), escaparJson(apellidos), escaparJson(correo), escaparJson(contrasena),
                    escaparJson(universidad), escaparJson(carrera), ciclo,
                    escaparJson(telefono != null ? telefono : ""));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/auth/register/estudiante"))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(5))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() == 201) {
                return null;
            }
            String errorMsg = extraerCadena(response.body(), "error");
            return (errorMsg != null && !errorMsg.isEmpty()) ? errorMsg
                    : "Error en el registro del estudiante (HTTP " + response.statusCode() + ")";
        } catch (Exception e) {
            return "No se pudo conectar con el servidor: " + e.getMessage();
        }
    }

    public static String registrarEmpresa(String nombre, String apellido, String razonSocial, String ruc,
            String correo, String contrasena, String telefono, String direccion, String descripcion) {
        try {
            String jsonBody = String.format(
                    "{\"nombre\":\"%s\",\"apellidos\":\"%s\",\"razonSocial\":\"%s\",\"ruc\":\"%s\",\"correo\":\"%s\","
                            + "\"contrasena\":\"%s\",\"telefono\":\"%s\",\"direccion\":\"%s\",\"descripcion\":\"%s\"}",
                    escaparJson(nombre),
                    escaparJson(apellido),
                    escaparJson(razonSocial),
                    escaparJson(ruc),
                    escaparJson(correo),
                    escaparJson(contrasena),
                    escaparJson(telefono != null ? telefono : ""),
                    escaparJson(direccion != null ? direccion : ""),
                    escaparJson(descripcion != null ? descripcion : ""));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/auth/register/empresa"))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(5))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            if (response.statusCode() == 201) {
                return null;
            }

            String errorMsg = extraerCadena(response.body(), "error");

            return (errorMsg != null && !errorMsg.isEmpty())
                    ? errorMsg
                    : "Error en el registro de la empresa (HTTP " + response.statusCode() + ")";

        } catch (Exception e) {
            return "No se pudo conectar con el servidor: " + e.getMessage();
        }
    }

    public static List<OfertaPractica> listarOfertasPublicas() {
        List<OfertaPractica> lista = new ArrayList<>();
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/auth/ofertas-publicas"))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() == 200) {
                List<String> items = dividirObjetosJsonArray(response.body());
                for (String item : items) {
                    int id = extraerEntero(item, "id");
                    String titulo = extraerCadena(item, "titulo");
                    String carrera = extraerCadena(item, "carrera");
                    String modalidad = extraerCadena(item, "modalidad");
                    int vacantes = extraerEntero(item, "vacantes");
                    String fecha = extraerCadena(item, "fechaLimite");
                    String estado = extraerCadena(item, "estado");
                    String empresa = extraerCadena(item, "razonSocial");

                    lista.add(new OfertaPractica(id, titulo, empresa, carrera, modalidad, vacantes, fecha,
                            estado != null && !estado.isEmpty() ? estado : "Activa"));
                }
            }
        } catch (Exception e) {
            System.err.println("Error en listarOfertasPublicas: " + e.getMessage());
        }
        return lista;
    }

    public static Map<String, Long> obtenerEstadisticasPublicas() {
        Map<String, Long> stats = new HashMap<>();
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/auth/estadisticas-publicas"))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() == 200) {
                String body = response.body();
                stats.put("estudiantes", (long) extraerEntero(body, "estudiantes"));
                stats.put("empresas", (long) extraerEntero(body, "empresas"));
                stats.put("ofertas", (long) extraerEntero(body, "ofertas"));
                stats.put("postulaciones", (long) extraerEntero(body, "postulaciones"));
            }
        } catch (Exception e) {
            System.err.println("Error en obtenerEstadisticasPublicas: " + e.getMessage());
        }
        return stats;
    }

    public static List<Estudiante> listarEstudiantes(String authHeader) {
        return listarEstudiantes();
    }

    public static List<Estudiante> listarEstudiantes() {
        List<Estudiante> lista = new ArrayList<>();
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/estudiantes"))
                    .header("X-ProPath-Internal-Key", INTERNAL_KEY)
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() == 200) {
                List<String> items = dividirObjetosJsonArray(response.body());
                for (String item : items) {
                    int id = extraerEntero(item, "id");
                    String nombre = extraerCadena(item, "nombre");
                    String apellidos = extraerCadena(item, "apellidos");
                    String correo = extraerCadena(item, "correo");
                    String universidad = extraerCadena(item, "universidad");
                    String carrera = extraerCadena(item, "carrera");
                    int ciclo = extraerEntero(item, "ciclo");
                    String telefono = extraerCadena(item, "telefono");
                    String cv = extraerCadena(item, "cv");
                    String estado = extraerCadena(item, "estado");

                    lista.add(new Estudiante(id, nombre, apellidos, correo, universidad, carrera, ciclo, telefono, cv,
                            estado != null && !estado.isEmpty() ? estado : "Activo"));
                }
            }
        } catch (Exception e) {
            System.err.println("Error en listarEstudiantes: " + e.getMessage());
        }
        return lista;
    }

    public static List<Empresa> listarEmpresas(String authHeader) {
        return listarEmpresas();
    }

    private static Empresa parsearEmpresaDesdeJson(String item) {
        if (item == null || item.trim().isEmpty()) {
            return null;
        }
        int uStart = item.indexOf("\"usuario\"");
        String userBlock = "";
        String rootItem = item;
        if (uStart != -1) {
            int braceStart = item.indexOf('{', uStart);
            if (braceStart != -1) {
                int depth = 0;
                int braceEnd = -1;
                for (int i = braceStart; i < item.length(); i++) {
                    if (item.charAt(i) == '{')
                        depth++;
                    else if (item.charAt(i) == '}') {
                        depth--;
                        if (depth == 0) {
                            braceEnd = i;
                            break;
                        }
                    }
                }
                if (braceEnd != -1) {
                    userBlock = item.substring(braceStart, braceEnd + 1);
                    rootItem = item.substring(0, uStart) + item.substring(braceEnd + 1);
                }
            }
        }

        int id = extraerEntero(rootItem, "id");
        String razonSocial = extraerCadena(rootItem, "razonSocial");
        String ruc = extraerCadena(rootItem, "ruc");
        String telefono = extraerCadena(rootItem, "telefono");
        String direccion = extraerCadena(rootItem, "direccion");
        String descripcion = extraerCadena(rootItem, "descripcion");
        String estado = extraerCadena(rootItem, "estado");

        String correo = extraerCadena(userBlock, "correo");
        String repNombre = extraerCadena(userBlock, "nombre");
        String repApellidos = extraerCadena(userBlock, "apellidos");
        int idUsuario = extraerEntero(userBlock, "id");
        String estadoCuenta = extraerCadena(userBlock, "estado");

        Empresa emp = new Empresa(id, razonSocial, ruc, correo, telefono, direccion, descripcion,
                estado != null && !estado.isEmpty() ? estado : "PENDIENTE", repNombre, repApellidos);
        emp.setIdUsuario(idUsuario);
        emp.setEstadoCuenta(estadoCuenta != null && !estadoCuenta.isEmpty() ? estadoCuenta : "ACTIVO");
        return emp;
    }

    public static List<Empresa> listarEmpresas() {
        List<Empresa> lista = new ArrayList<>();
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/empresas"))
                    .header("X-ProPath-Internal-Key", INTERNAL_KEY)
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() == 200) {
                List<String> items = dividirObjetosJsonArray(response.body());
                for (String item : items) {
                    Empresa emp = parsearEmpresaDesdeJson(item);
                    if (emp != null) {
                        lista.add(emp);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error en listarEmpresas: " + e.getMessage());
        }
        return lista;
    }

    public static Empresa obtenerEmpresaPorId(int idEmpresa) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/empresas/" + idEmpresa))
                    .header("X-ProPath-Internal-Key", INTERNAL_KEY)
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() == 200) {
                return parsearEmpresaDesdeJson(response.body());
            }
        } catch (Exception e) {
            System.err.println("Error en obtenerEmpresaPorId: " + e.getMessage());
        }
        return null;
    }

    private static String extraerCampoOferta(String json, String clave) {
        if (json == null || clave == null) {
            return "";
        }
        int nivel = 0;
        boolean dentroCadena = false;
        boolean escapado = false;

        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);

            if (dentroCadena) {
                if (escapado) {
                    escapado = false;
                } else if (c == '\\') {
                    escapado = true;
                } else if (c == '"') {
                    dentroCadena = false;
                }
                continue;
            }

            if (c == '"') {
                if (nivel == 1) {
                    int inicio = i + 1;
                    int fin = json.indexOf('"', inicio);

                    if (fin > inicio) {
                        String nombreCampo = json.substring(inicio, fin);

                        if (nombreCampo.equals(clave)) {
                            int pos = fin + 1;

                            while (pos < json.length()
                                    && Character.isWhitespace(json.charAt(pos))) {
                                pos++;
                            }

                            if (pos < json.length() && json.charAt(pos) == ':') {
                                pos++;

                                while (pos < json.length()
                                        && Character.isWhitespace(json.charAt(pos))) {
                                    pos++;
                                }

                                if (pos < json.length() && json.charAt(pos) == '"') {
                                    pos++;
                                    StringBuilder valor = new StringBuilder();
                                    boolean escape = false;

                                    while (pos < json.length()) {
                                        char ch = json.charAt(pos++);

                                        if (escape) {
                                            switch (ch) {
                                                case 'n':
                                                    valor.append('\n');
                                                    break;
                                                case 'r':
                                                    valor.append('\r');
                                                    break;
                                                case 't':
                                                    valor.append('\t');
                                                    break;
                                                case '"':
                                                    valor.append('"');
                                                    break;
                                                case '\\':
                                                    valor.append('\\');
                                                    break;
                                                default:
                                                    valor.append(ch);
                                            }
                                            escape = false;
                                        } else if (ch == '\\') {
                                            escape = true;
                                        } else if (ch == '"') {
                                            return valor.toString();
                                        } else {
                                            valor.append(ch);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                dentroCadena = true;
            } else if (c == '{') {
                nivel++;
            } else if (c == '}') {
                nivel--;
            }
        }

        return "";
    }

    public static List<OfertaPractica> listarOfertas(String authHeader) {
        return listarOfertas();
    }

    public static List<OfertaPractica> listarOfertas() {
        List<OfertaPractica> lista = new ArrayList<>();
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/ofertas"))
                    .header("X-ProPath-Internal-Key", INTERNAL_KEY)
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() == 200) {
                List<String> items = dividirObjetosJsonArray(response.body());
                for (String item : items) {
                    int id = extraerEntero(item, "id");
                    String titulo = extraerCadena(item, "titulo");
                    String carrera = extraerCadena(item, "carrera");
                    String modalidad = extraerCadena(item, "modalidad");
                    int vacantes = extraerEntero(item, "vacantes");
                    String fecha = extraerCadena(item, "fechaLimite");
                    String estado = extraerCampoOferta(item, "estado");
                    String empresa = extraerCadena(item, "razonSocial");
                    String descripcion = extraerCampoOferta(item, "descripcion");
                    String requisitos = extraerCampoOferta(item, "requisitos");
                    String ubicacion = extraerCampoOferta(item, "ubicacion");
                    String remuneracion = extraerCampoOferta(item, "remuneracion");

                    OfertaPractica oferta = new OfertaPractica(
                            id, titulo, empresa, carrera, modalidad, vacantes, fecha,
                            estado != null && !estado.isEmpty() ? estado : "PENDIENTE");

                    oferta.setFechaLimite(fecha);
                    oferta.setDescripcion(descripcion);
                    oferta.setRequisitos(requisitos);
                    oferta.setUbicacion(ubicacion);
                    oferta.setRemuneracion(remuneracion);
                    lista.add(oferta);
                }
            }
        } catch (Exception e) {
            System.err.println("Error en listarOfertas: " + e.getMessage());
        }
        return lista;
    }

    public static List<Postulacion> listarPostulaciones(String authHeader) {
        return listarPostulaciones();
    }

    public static List<Postulacion> listarPostulaciones() {
        List<Postulacion> lista = new ArrayList<>();
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/postulaciones"))
                    .header("X-ProPath-Internal-Key", INTERNAL_KEY)
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() == 200) {
                List<String> items = dividirObjetosJsonArray(response.body());
                for (String item : items) {
                    int id = extraerEntero(item, "id");
                    String estado = extraerCampoOferta(item, "estado");
                    String fecha = extraerCadena(item, "fechaPostulacion");
                    String estudiante = extraerCadena(item, "nombre");
                    String oferta = extraerCadena(item, "titulo");
                    String empresa = extraerCadena(item, "razonSocial");

                    lista.add(new Postulacion(id, estudiante, oferta, empresa, fecha,
                            estado != null && !estado.isEmpty() ? estado : "Postulado"));
                }
            }
        } catch (Exception e) {
            System.err.println("Error en listarPostulaciones: " + e.getMessage());
        }
        return lista;
    }

    public static List<Usuario> listarUsuarios() {
        List<Usuario> lista = new ArrayList<>();
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/usuarios"))
                    .header("X-ProPath-Internal-Key", INTERNAL_KEY)
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() == 200) {
                List<String> items = dividirObjetosJsonArray(response.body());
                for (String item : items) {
                    int id = extraerEntero(item, "id");
                    String nombre = extraerCadena(item, "nombre");
                    String apellidos = extraerCadena(item, "apellidos");
                    String correo = extraerCadena(item, "correo");
                    String rol = extraerCadena(item, "rol");
                    String estado = extraerCadena(item, "estado");
                    String fechaRegistro = extraerCadena(item, "fechaRegistro");
                    Usuario u = new Usuario(id, nombre, apellidos != null ? apellidos : "", correo, rol, estado);
                    if (fechaRegistro != null && !fechaRegistro.isEmpty()) {
                        u.setFechaRegistro(fechaRegistro.replace("T", " "));
                    }
                    lista.add(u);
                }
            }
        } catch (Exception e) {
            System.err.println("Error en listarUsuarios: " + e.getMessage());
        }
        return lista;
    }

    public static boolean actualizarEstadoEmpresa(int id, String nuevoEstado) {
        try {
            String jsonBody = String.format("{\"estado\":\"%s\"}", nuevoEstado.toUpperCase().trim());
            HttpRequest putReq = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/empresas/" + id + "/estado"))
                    .header("X-ProPath-Internal-Key", INTERNAL_KEY)
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> putRes = client.send(putReq,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            return putRes.statusCode() == 200;
        } catch (Exception e) {
            System.err.println("Error en actualizarEstadoEmpresa: " + e.getMessage());
            return false;
        }
    }

    public static Map<String, Object> obtenerEstadisticasAdmin() {
        Map<String, Object> map = new HashMap<>();
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/admin/estadisticas"))
                    .header("X-ProPath-Internal-Key", INTERNAL_KEY)
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() == 200) {
                String body = response.body();
                map.put("totalEstudiantes", (long) extraerEntero(body, "totalEstudiantes"));
                map.put("estudiantesEstaSemana", (long) extraerEntero(body, "estudiantesEstaSemana"));
                map.put("totalEmpresas", (long) extraerEntero(body, "totalEmpresas"));
                map.put("empresasAprobadas", (long) extraerEntero(body, "empresasAprobadas"));
                map.put("empresasPendientes", (long) extraerEntero(body, "empresasPendientes"));
                map.put("totalOfertas", (long) extraerEntero(body, "totalOfertas"));
                map.put("ofertasActivas", (long) extraerEntero(body, "ofertasActivas"));
                map.put("ofertasPendientes", (long) extraerEntero(body, "ofertasPendientes"));
                map.put("totalPostulaciones", (long) extraerEntero(body, "totalPostulaciones"));
                map.put("postulacionesEnRevision", (long) extraerEntero(body, "postulacionesEnRevision"));
                map.put("postulacionesSeleccionadas", (long) extraerEntero(body, "postulacionesSeleccionadas"));
            }
        } catch (Exception e) {
            System.err.println("Error en obtenerEstadisticasAdmin: " + e.getMessage());
        }
        return map;
    }

    public static boolean actualizarEstadoEstudiante(int id, String nuevoEstado) {
        try {
            String jsonBody = String.format("{\"estado\":\"%s\"}", nuevoEstado.toUpperCase().trim());
            HttpRequest putReq = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/estudiantes/" + id + "/estado"))
                    .header("X-ProPath-Internal-Key", INTERNAL_KEY)
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> putRes = client.send(putReq,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            return putRes.statusCode() == 200;
        } catch (Exception e) {
            System.err.println("Error en actualizarEstadoEstudiante: " + e.getMessage());
            return false;
        }
    }

    public static DocumentoEstudiante obtenerCvEstudiante(int idEstudiante) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/estudiantes/" + idEstudiante + "/documentos/cv"))
                    .header("X-ProPath-Internal-Key", INTERNAL_KEY)
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();

            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (res.statusCode() == 200) {
                String body = res.body();
                int idDoc = extraerEntero(body, "id");
                String nombreArchivo = extraerCadena(body, "nombreArchivo");
                int tamInt = extraerEntero(body, "tamanioBytes");
                long tam = (long) tamInt;
                String cType = extraerCadena(body, "contentType");
                String fecha = extraerCadena(body, "fechaSubida");

                return new DocumentoEstudiante(idDoc, idEstudiante, "CV", nombreArchivo, tam, cType, fecha);
            }
        } catch (Exception e) {
            System.err.println("Error en obtenerCvEstudiante: " + e.getMessage());
        }
        return null;
    }

    public static String guardarCvEstudiante(int idEstudiante, String nombreOriginal, byte[] contenidoPdf) {
        try {
            String boundary = "---ProPathBoundary" + System.currentTimeMillis();
            String nombreSeguro = nombreOriginal != null ? nombreOriginal.replace("\"", "") : "CV.pdf";

            byte[] headerBytes = ("--" + boundary + "\r\n" +
                    "Content-Disposition: form-data; name=\"archivo\"; filename=\"" + nombreSeguro + "\"\r\n" +
                    "Content-Type: application/pdf\r\n\r\n").getBytes(StandardCharsets.UTF_8);
            byte[] footerBytes = ("\r\n--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8);

            byte[] fullBody = new byte[headerBytes.length + contenidoPdf.length + footerBytes.length];
            System.arraycopy(headerBytes, 0, fullBody, 0, headerBytes.length);
            System.arraycopy(contenidoPdf, 0, fullBody, headerBytes.length, contenidoPdf.length);
            System.arraycopy(footerBytes, 0, fullBody, headerBytes.length + contenidoPdf.length, footerBytes.length);

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/estudiantes/" + idEstudiante + "/documentos/cv"))
                    .header("X-ProPath-Internal-Key", INTERNAL_KEY)
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .timeout(Duration.ofSeconds(15))
                    .POST(HttpRequest.BodyPublishers.ofByteArray(fullBody))
                    .build();

            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (res.statusCode() == 200 || res.statusCode() == 201) {
                return null;
            }
            String err = extraerCadena(res.body(), "error");
            return (err != null && !err.isEmpty()) ? err : ("Error al guardar CV (HTTP " + res.statusCode() + ")");
        } catch (Exception e) {
            return "No se pudo conectar con el servidor: " + e.getMessage();
        }
    }

    public static byte[] descargarCvEstudiante(int idEstudiante, boolean inline) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(
                            BASE_URL + "/estudiantes/" + idEstudiante + "/documentos/cv/descargar?inline=" + inline))
                    .header("X-ProPath-Internal-Key", INTERNAL_KEY)
                    .timeout(Duration.ofSeconds(15))
                    .GET()
                    .build();

            HttpResponse<byte[]> res = client.send(req, HttpResponse.BodyHandlers.ofByteArray());
            if (res.statusCode() == 200) {
                return res.body();
            }
        } catch (Exception e) {
            System.err.println("Error en descargarCvEstudiante: " + e.getMessage());
        }
        return null;
    }

    public static boolean eliminarCvEstudiante(int idEstudiante) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/estudiantes/" + idEstudiante + "/documentos/cv"))
                    .header("X-ProPath-Internal-Key", INTERNAL_KEY)
                    .timeout(Duration.ofSeconds(5))
                    .DELETE()
                    .build();

            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            return res.statusCode() == 204 || res.statusCode() == 200;
        } catch (Exception e) {
            System.err.println("Error en eliminarCvEstudiante: " + e.getMessage());
            return false;
        }
    }

    public static boolean actualizarEstadoOferta(int id, String nuevoEstado) {
        try {
            String jsonBody = String.format(
                    "{\"estado\":\"%s\"}",
                    escaparJson(nuevoEstado.toUpperCase().trim()));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/ofertas/" + id + "/estado"))
                    .header("X-ProPath-Internal-Key", INTERNAL_KEY)
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(5))
                    .PUT(HttpRequest.BodyPublishers.ofString(
                            jsonBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            return response.statusCode() == 200;

        } catch (Exception e) {
            System.err.println(
                    "Error en actualizarEstadoOferta: " + e.getMessage());
            return false;
        }
    }

    public static boolean actualizarEstadoPostulacion(int id, String nuevoEstado) {
        try {
            String jsonBody = String.format(
                    "{\"estado\":\"%s\"}",
                    escaparJson(nuevoEstado.toUpperCase().trim()));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/postulaciones/" + id))
                    .header("X-ProPath-Internal-Key", INTERNAL_KEY)
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(5))
                    .PUT(HttpRequest.BodyPublishers.ofString(
                            jsonBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            return response.statusCode() == 200;

        } catch (Exception e) {
            System.err.println(
                    "Error en actualizarEstadoPostulacion: " + e.getMessage());
            return false;
        }
    }

    public static boolean eliminarRegistro(String recurso, int id) {
        String err = eliminarRegistroConDetalle(recurso, id);
        return err == null;
    }

    public static String eliminarRegistroConDetalle(String recurso, int id) {
        try {
            HttpRequest delReq = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/" + recurso + "/" + id))
                    .header("X-ProPath-Internal-Key", INTERNAL_KEY)
                    .DELETE()
                    .build();
            HttpResponse<String> delRes = client.send(delReq,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (delRes.statusCode() == 204 || delRes.statusCode() == 200) {
                return null;
            }
            String err = extraerCadena(delRes.body(), "error");
            if (err == null || err.isEmpty()) {
                err = extraerCadena(delRes.body(), "message");
            }
            return err != null && !err.isEmpty() ? err
                    : "Error al eliminar registro (HTTP " + delRes.statusCode() + ")";
        } catch (Exception e) {
            System.err.println("Error en eliminarRegistro " + recurso + "/" + id + ": " + e.getMessage());
            return "Error de conexión: " + e.getMessage();
        }
    }

    public static String activarCuentaEmpresa(int idEmpresa) {
        try {
            HttpRequest putReq = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/empresas/" + idEmpresa + "/estado-cuenta"))
                    .header("X-ProPath-Internal-Key", INTERNAL_KEY)
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString("{\"estado\":\"ACTIVO\"}", StandardCharsets.UTF_8))
                    .build();
            HttpResponse<String> res = client.send(putReq, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (res.statusCode() == 200) {
                return null;
            }
            String err = extraerCadena(res.body(), "error");
            if (err == null || err.isEmpty()) {
                err = extraerCadena(res.body(), "message");
            }
            return err != null && !err.isEmpty() ? err : "Error al activar la cuenta (HTTP " + res.statusCode() + ")";
        } catch (Exception e) {
            return "Error de conexión: " + e.getMessage();
        }
    }

    public static String obtenerRespuestasPostulacion(
            int idPostulacion, int idEstudiante) {

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/postulaciones/"
                            + idPostulacion + "/respuestas?idEstudiante="
                            + idEstudiante))
                    .header("X-ProPath-Internal-Key", INTERNAL_KEY)
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            return response.statusCode() == 200 ? response.body() : null;

        } catch (Exception e) {
            return null;
        }
    }

    public static String postular(
            int idEstudiante,
            int idOferta,
            String[] respuestas) {

        if (respuestas == null || respuestas.length != 12) {
            return "Debes responder las 12 preguntas.";
        }

        try {
            StringBuilder json = new StringBuilder();

            json.append("{\"estudiante\":{\"id\":")
                    .append(idEstudiante)
                    .append("},\"oferta\":{\"id\":")
                    .append(idOferta)
                    .append("},\"respuestas\":[");

            for (int i = 0; i < respuestas.length; i++) {
                if (respuestas[i] == null ||
                        respuestas[i].trim().isEmpty()) {
                    return "Todas las preguntas son obligatorias.";
                }

                if (i > 0) {
                    json.append(",");
                }

                json.append("\"")
                        .append(escaparJson(respuestas[i].trim()))
                        .append("\"");
            }

            json.append("]}");

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/postulaciones"))
                    .header("X-ProPath-Internal-Key", INTERNAL_KEY)
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(15))
                    .POST(HttpRequest.BodyPublishers.ofString(
                            json.toString(), StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            if (response.statusCode() == 201) {
                return null;
            }

            String error = extraerCadena(response.body(), "detail");

            if (error == null || error.isEmpty()) {
                error = extraerCadena(response.body(), "message");
            }

            return error != null && !error.isEmpty()
                    ? error
                    : "No se pudo registrar la postulación (HTTP "
                            + response.statusCode() + ")";

        } catch (Exception e) {
            return "Error de conexión: " + e.getMessage();
        }
    }

    public static String actualizarRespuestasPostulacion(
            int idPostulacion,
            int idEstudiante,
            String[] respuestas) {

        if (respuestas == null || respuestas.length != 12) {
            return "Debes completar las 12 respuestas.";
        }

        try {
            StringBuilder json = new StringBuilder();
            json.append("{\"idEstudiante\":")
                    .append(idEstudiante)
                    .append(",\"respuestas\":[");

            for (int i = 0; i < 12; i++) {
                if (respuestas[i] == null
                        || respuestas[i].trim().isEmpty()
                        || respuestas[i].trim().length() > 1000) {
                    return "Todas las respuestas son obligatorias.";
                }

                if (i > 0) {
                    json.append(",");
                }

                json.append("\"")
                        .append(escaparJson(respuestas[i].trim()))
                        .append("\"");
            }

            json.append("]}");

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/postulaciones/"
                            + idPostulacion + "/respuestas"))
                    .header("X-ProPath-Internal-Key", INTERNAL_KEY)
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(15))
                    .PUT(HttpRequest.BodyPublishers.ofString(
                            json.toString(), StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            if (response.statusCode() == 200) {
                return null;
            }

            return "No se pudieron actualizar las respuestas (HTTP "
                    + response.statusCode() + ")";

        } catch (Exception e) {
            return "Error de conexión: " + e.getMessage();
        }
    }

    private static String normalizarModalidad(String modalidad) {
        if (modalidad == null || modalidad.trim().isEmpty()) {
            return "PRESENCIAL";
        }

        String valor = java.text.Normalizer.normalize(
                modalidad.trim(),
                java.text.Normalizer.Form.NFD).replaceAll("\\p{M}", "")
                .toUpperCase(java.util.Locale.ROOT);

        switch (valor) {
            case "PRESENCIAL":
                return "PRESENCIAL";

            case "HIBRIDA":
            case "HIBRIDO":
                return "HIBRIDA";

            case "REMOTA":
            case "REMOTO":
            case "VIRTUAL":
                return "VIRTUAL";

            default:
                throw new IllegalArgumentException(
                        "Modalidad no reconocida: " + modalidad);
        }
    }

    public static String publicarOferta(int idEmpresa, String titulo, String descripcion, String requisitos,
            String carrera, String modalidad, int vacantes, String fechaLimite,
            String ubicacion, String remuneracion) {

        try {
            String jsonBody = String.format(
                    "{\"empresa\":{\"id\":%d},\"titulo\":\"%s\","
                            + "\"descripcion\":\"%s\",\"requisitos\":\"%s\","
                            + "\"carrera\":\"%s\",\"modalidad\":\"%s\","
                            + "\"vacantes\":%d,\"fechaLimite\":\"%s\","
                            + "\"ubicacion\":\"%s\",\"remuneracion\":\"%s\"}",
                    idEmpresa,
                    escaparJson(titulo),
                    escaparJson(descripcion),
                    escaparJson(requisitos),
                    escaparJson(carrera),
                    escaparJson(normalizarModalidad(modalidad)),
                    vacantes,
                    escaparJson(fechaLimite),
                    escaparJson(ubicacion),
                    escaparJson(remuneracion != null && !remuneracion.trim().isEmpty()
                            ? "S/ " + remuneracion.trim().replaceFirst("^S/\\s*", "")
                            : ""));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/ofertas"))
                    .header("X-ProPath-Internal-Key", INTERNAL_KEY)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() == 201) {
                return null;
            }
            String errorMsg = extraerCadena(response.body(), "error");
            return (errorMsg != null && !errorMsg.isEmpty()) ? errorMsg
                    : "No se pudo registrar la oferta (HTTP " + response.statusCode() + ")";
        } catch (Exception e) {
            return "Error de conexión: " + e.getMessage();
        }
    }

    public static String crearUsuario(String nombre, String correo, String contrasena, String rol, String estado) {
        return crearUsuario(nombre, "", correo, contrasena, rol, estado);
    }

    public static String crearUsuario(String nombre, String apellidos, String correo, String contrasena, String rol,
            String estado) {
        try {
            String jsonBody = String.format(
                    "{\"nombre\":\"%s\",\"apellidos\":\"%s\",\"correo\":\"%s\",\"contrasena\":\"%s\",\"rol\":\"%s\",\"estado\":\"%s\"}",
                    escaparJson(nombre), escaparJson(apellidos != null ? apellidos : ""), escaparJson(correo),
                    escaparJson(contrasena),
                    escaparJson(rol != null ? rol.toUpperCase() : "ESTUDIANTE"),
                    escaparJson(estado != null ? estado.toUpperCase() : "ACTIVO"));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/usuarios"))
                    .header("X-ProPath-Internal-Key", INTERNAL_KEY)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() == 201 || response.statusCode() == 200) {
                return null;
            }
            String errorMsg = extraerCadena(response.body(), "error");
            return (errorMsg != null && !errorMsg.isEmpty()) ? errorMsg
                    : "Error al crear usuario (HTTP " + response.statusCode() + ")";
        } catch (Exception e) {
            return "Error de conexión: " + e.getMessage();
        }
    }

    public static String actualizarUsuario(int id, String nombre, String apellidos, String correo, String contrasena,
            String estado) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("{");
            sb.append(String.format(
                    "\"id\":%d,\"nombre\":\"%s\",\"apellidos\":\"%s\",\"correo\":\"%s\",\"estado\":\"%s\"",
                    id, escaparJson(nombre), escaparJson(apellidos != null ? apellidos : ""), escaparJson(correo),
                    escaparJson(estado != null ? estado.toUpperCase() : "ACTIVO")));
            if (contrasena != null && !contrasena.trim().isEmpty()) {
                sb.append(String.format(",\"contrasena\":\"%s\"", escaparJson(contrasena.trim())));
            }
            sb.append("}");

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/usuarios/" + id))
                    .header("X-ProPath-Internal-Key", INTERNAL_KEY)
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(sb.toString(), StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() == 200) {
                return null;
            }
            String errorMsg = extraerCadena(response.body(), "error");
            return (errorMsg != null && !errorMsg.isEmpty()) ? errorMsg
                    : "Error al actualizar usuario (HTTP " + response.statusCode() + ")";
        } catch (Exception e) {
            return "Error de conexión: " + e.getMessage();
        }
    }

    public static String crearEmpresaAdmin(String nombre, String razonSocial, String ruc, String correo,
            String contrasena, String telefono, String direccion, String descripcion, String estado) {
        String err = registrarEmpresa(nombre, razonSocial, ruc, correo, contrasena, telefono, direccion, descripcion);
        if (err == null && estado != null && !"PENDIENTE".equalsIgnoreCase(estado)) {
            List<Empresa> emps = listarEmpresas();
            for (Empresa emp : emps) {
                if (emp.getRuc() != null && emp.getRuc().equalsIgnoreCase(ruc.trim())) {
                    actualizarEstadoEmpresa(emp.getId(), estado);
                    break;
                }
            }
        }
        return err;
    }

    public static String registrarEmpresa(String nombre, String razonSocial, String ruc, String correo,
            String contrasena, String telefono, String direccion, String descripcion) {

        return registrarEmpresa(nombre, "", razonSocial, ruc, correo,
                contrasena, telefono, direccion, descripcion);
    }

    public static String actualizarEmpresa(int id, String razonSocial, String ruc, String telefono, String estado) {
        return actualizarEmpresa(id, razonSocial, ruc, telefono, "", "", estado);
    }

    public static String actualizarEmpresa(int id, String razonSocial, String ruc, String telefono, String direccion,
            String descripcion, String estado) {
        return actualizarEmpresa(id, razonSocial, ruc, telefono, direccion, descripcion, estado, null, null, null,
                null);
    }

    public static String actualizarEmpresa(int id, String razonSocial, String ruc, String telefono, String direccion,
            String descripcion, String estado, String repNombre, String repApellidos, String repCorreo,
            String repContrasena) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("{");
            sb.append(String.format(
                    "\"id\":%d,\"razonSocial\":\"%s\",\"ruc\":\"%s\",\"telefono\":\"%s\",\"direccion\":\"%s\",\"descripcion\":\"%s\"",
                    id, escaparJson(razonSocial), escaparJson(ruc), escaparJson(telefono != null ? telefono : ""),
                    escaparJson(direccion != null ? direccion : ""),
                    escaparJson(descripcion != null ? descripcion : "")));

            if (estado != null && !estado.trim().isEmpty() &&
                    !"ACTIVO".equalsIgnoreCase(estado) && !"INACTIVO".equalsIgnoreCase(estado)
                    && !"BLOQUEADO".equalsIgnoreCase(estado)) {
                sb.append(String.format(",\"estado\":\"%s\"", escaparJson(estado.toUpperCase().trim())));
            }

            if ((repNombre != null && !repNombre.trim().isEmpty()) ||
                    (repApellidos != null && !repApellidos.trim().isEmpty()) ||
                    (repCorreo != null && !repCorreo.trim().isEmpty()) ||
                    (repContrasena != null && !repContrasena.trim().isEmpty())) {

                sb.append(",\"usuario\":{");
                sb.append(String.format("\"nombre\":\"%s\",\"apellidos\":\"%s\",\"correo\":\"%s\"",
                        escaparJson(repNombre != null ? repNombre.trim() : ""),
                        escaparJson(repApellidos != null ? repApellidos.trim() : ""),
                        escaparJson(repCorreo != null ? repCorreo.trim() : "")));
                if (repContrasena != null && !repContrasena.trim().isEmpty()) {
                    sb.append(String.format(",\"contrasena\":\"%s\"", escaparJson(repContrasena.trim())));
                }
                sb.append("}");
            }
            sb.append("}");

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/empresas/" + id))
                    .header("X-ProPath-Internal-Key", INTERNAL_KEY)
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(sb.toString(), StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() == 200) {
                return null;
            }
            String errorMsg = extraerCadena(response.body(), "error");
            if (errorMsg == null || errorMsg.isEmpty()) {
                errorMsg = extraerCadena(response.body(), "message");
            }
            return (errorMsg != null && !errorMsg.isEmpty()) ? errorMsg
                    : "Error al actualizar empresa (HTTP " + response.statusCode() + ")";
        } catch (Exception e) {
            return "Error de conexión: " + e.getMessage();
        }
    }

    public static String actualizarOferta(
            int id, int idEmpresa, String titulo, String descripcion,
            String requisitos, String carrera, String modalidad,
            int vacantes, String fechaLimite, String ubicacion,
            String remuneracion, String estado) {

        try {
            String jsonBody = String.format(
                    "{\"id\":%d,\"empresa\":{\"id\":%d}," +
                            "\"titulo\":\"%s\"," +
                            "\"descripcion\":\"%s\"," +
                            "\"requisitos\":\"%s\"," +
                            "\"carrera\":\"%s\"," +
                            "\"modalidad\":\"%s\"," +
                            "\"vacantes\":%d," +
                            "\"fechaLimite\":\"%s\"," +
                            "\"ubicacion\":\"%s\"," +
                            "\"remuneracion\":\"%s\"," +
                            "\"estado\":\"%s\"}",

                    id,
                    idEmpresa,
                    escaparJson(titulo),
                    escaparJson(descripcion),
                    escaparJson(requisitos),
                    escaparJson(carrera),
                    escaparJson(normalizarModalidad(modalidad)),
                    vacantes,
                    escaparJson(fechaLimite),
                    escaparJson(ubicacion),
                    escaparJson(remuneracion != null && !remuneracion.trim().isEmpty()
                            ? "S/ " + remuneracion.trim().replaceFirst("^S/\\s*", "")
                            : ""),
                    escaparJson(estado));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/ofertas/" + id))
                    .header("X-ProPath-Internal-Key", INTERNAL_KEY)
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(
                            jsonBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            if (response.statusCode() == 200) {
                return null;
            }

            String errorMsg = extraerCadena(response.body(), "error");

            return errorMsg != null && !errorMsg.isEmpty()
                    ? errorMsg
                    : "No se pudo actualizar la oferta (HTTP "
                            + response.statusCode() + ")";

        } catch (Exception e) {
            return "Error de conexión: " + e.getMessage();
        }
    }

    public static String actualizarPerfilEstudiante(int idEstudiante, String nombre, String apellidos,
            String universidad, String carrera, int ciclo, String telefono) {
        try {
            String jsonBody = String.format("{\"id\":%d,\"usuario\":{\"nombre\":\"%s\"},\"apellidos\":\"%s\"," +
                    "\"universidad\":\"%s\",\"carrera\":\"%s\",\"ciclo\":%d,\"telefono\":\"%s\"}",
                    idEstudiante, escaparJson(nombre), escaparJson(apellidos),
                    escaparJson(universidad), escaparJson(carrera), ciclo,
                    escaparJson(telefono != null ? telefono : ""));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/estudiantes/" + idEstudiante))
                    .header("X-ProPath-Internal-Key", INTERNAL_KEY)
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() == 200) {
                return null;
            }
            String errorMsg = extraerCadena(response.body(), "error");
            return (errorMsg != null && !errorMsg.isEmpty()) ? errorMsg
                    : "No se pudo actualizar el perfil (HTTP " + response.statusCode() + ")";
        } catch (Exception e) {
            return "Error de conexión: " + e.getMessage();
        }
    }

    public static Estudiante obtenerEstudiantePorId(int idEstudiante) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/estudiantes/" + idEstudiante))
                    .header("X-ProPath-Internal-Key", INTERNAL_KEY)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() == 200) {
                String body = response.body();
                int id = extraerEntero(body, "id");
                String nombre = extraerCadena(body, "nombre");
                String apellidos = extraerCadena(body, "apellidos");
                String correo = extraerCadena(body, "correo");
                String universidad = extraerCadena(body, "universidad");
                String carrera = extraerCadena(body, "carrera");
                int ciclo = extraerEntero(body, "ciclo");
                String telefono = extraerCadena(body, "telefono");
                String cv = extraerCadena(body, "cv");
                String estado = extraerCadena(body, "estado");

                return new Estudiante(id, nombre, apellidos, correo, universidad, carrera, ciclo, telefono, cv,
                        estado != null && !estado.isEmpty() ? estado : "Activo");
            }
        } catch (Exception e) {
            System.err.println("Error en obtenerEstudiantePorId: " + e.getMessage());
        }
        return null;
    }

    private static String escaparJson(String s) {
        if (s == null) {
            return "";
        }

        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n")
                .replace("\t", "\\t");
    }

    private static String extraerCadena(String json, String clave) {
        Pattern pattern = Pattern.compile("\"" + clave + "\"\\s*:\\s*\"([^\"]*)\"");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }

    private static int extraerEntero(String json, String clave) {
        Pattern pattern = Pattern.compile("\"" + clave + "\"\\s*:\\s*([0-9]+)");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException ignored) {
            }
        }
        return 0;
    }

    private static List<String> dividirObjetosJsonArray(String jsonArray) {
        List<String> objetos = new ArrayList<>();
        if (jsonArray == null || !jsonArray.trim().startsWith("["))
            return objetos;

        int nivel = 0;
        StringBuilder sb = new StringBuilder();
        for (char c : jsonArray.toCharArray()) {
            if (c == '{') {
                nivel++;
                sb.append(c);
            } else if (c == '}') {
                nivel--;
                sb.append(c);
                if (nivel == 0) {
                    objetos.add(sb.toString());
                    sb.setLength(0);
                }
            } else if (nivel > 0) {
                sb.append(c);
            }
        }
        return objetos;
    }
}
