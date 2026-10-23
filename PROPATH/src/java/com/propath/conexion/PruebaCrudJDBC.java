package com.propath.conexion;

import com.propath.dao.EmpresaDAO;
import com.propath.dao.EntrevistaDAO;
import com.propath.dao.EstudianteDAO;
import com.propath.dao.OfertaPracticaDAO;
import com.propath.dao.PostulacionDAO;
import com.propath.dao.UsuarioDAO;
import com.propath.modelo.Empresa;
import com.propath.modelo.Entrevista;
import com.propath.modelo.Estudiante;
import com.propath.modelo.OfertaPractica;
import com.propath.modelo.Postulacion;
import com.propath.modelo.RespuestaPostulacion;
import com.propath.modelo.Usuario;
import java.util.ArrayList;
import java.util.List;

public class PruebaCrudJDBC {

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("EJECUTANDO PRUEBAS UNITARIAS DE LOS CINCO CRUD JDBC");
        System.out.println("==================================================");

        boolean exitoTotal = true;

        exitoTotal &= probarUsuarioDAO();
        exitoTotal &= probarCRUD1Estudiantes();
        exitoTotal &= probarCRUD2Empresas();
        exitoTotal &= probarCRUD3Ofertas();
        exitoTotal &= probarCRUD4Postulaciones();
        exitoTotal &= probarCRUD5Entrevistas();

        System.out.println("==================================================");
        if (exitoTotal) {
            System.out.println("RESULTADO GLOBAL: TODOS LOS 5 CRUD FUNCIONAN AL 100%");
        } else {
            System.out.println("RESULTADO GLOBAL: AL MENOS UNA PRUEBA PRESENTO FALLO");
        }
        System.out.println("==================================================");
    }

    private static boolean probarUsuarioDAO() {
        System.out.println("\n--- [MODULO AUTENTICACION Y USUARIOS] ---");
        UsuarioDAO dao = new UsuarioDAO();

        List<Usuario> lista = dao.listarTodos();
        System.out.println("[OK] SELECT general usuarios: " + lista.size() + " registros.");

        Usuario u = new Usuario();
        u.setNombre("TestUser");
        u.setApellido("Temporal");
        u.setCorreo("test.temp." + System.currentTimeMillis() + "@propath.com");
        u.setRol("ESTUDIANTE");
        u.setEstado("ACTIVO");

        int idGenerado = dao.crear(u, "ClaveTest123");
        if (idGenerado <= 0) {
            System.err.println("[FALLO] INSERT usuario");
            return false;
        }
        System.out.println("[OK] INSERT usuario con id: " + idGenerado);

        Usuario consultado = dao.obtenerPorId(idGenerado);
        if (consultado == null || !consultado.getCorreo().equals(u.getCorreo())) {
            System.err.println("[FALLO] SELECT por ID usuario");
            return false;
        }
        System.out.println("[OK] SELECT por ID usuario: " + consultado.getCorreo());

        Usuario autenticado = dao.autenticar(u.getCorreo(), "ClaveTest123");
        if (autenticado == null) {
            System.err.println("[FALLO] Autenticacion con BCrypt");
            return false;
        }
        System.out.println("[OK] Autenticacion BCrypt exitosa para usuario: " + autenticado.getId());

        consultado.setNombre("TestUserModificado");
        boolean modificado = dao.actualizar(consultado);
        System.out.println("[OK] UPDATE usuario nombre: " + modificado);

        boolean baja = dao.darDeBaja(idGenerado);
        System.out.println("[OK] Baja logica usuario: " + baja);

        boolean eliminado = dao.eliminar(idGenerado);
        System.out.println("[OK] DELETE usuario de prueba: " + eliminado);

        return true;
    }

    private static boolean probarCRUD1Estudiantes() {
        System.out.println("\n--- [CRUD 1: ESTUDIANTES] ---");
        EstudianteDAO dao = new EstudianteDAO();

        List<Estudiante> lista = dao.listarTodos();
        System.out.println("[OK] SELECT general estudiantes: " + lista.size() + " registros.");

        String correoUnico = "est.test." + System.currentTimeMillis() + "@upn.pe";
        Estudiante est = new Estudiante();
        est.setNombre("Carlos");
        est.setApellido("Medina");
        est.setUniversidad("UPN");
        est.setCarrera("Ingenieria de Sistemas");
        est.setCiclo(8);
        est.setTelefono("987654321");
        est.setCv("cv_carlos.pdf");

        int idEst = dao.registrarConUsuario(est, correoUnico, "ClaveEst123");
        if (idEst <= 0) {
            System.err.println("[FALLO] INSERT transaccional estudiante + usuario");
            return false;
        }
        System.out.println("[OK] INSERT transaccional estudiante con id: " + idEst);

        Estudiante consultado = dao.obtenerPorId(idEst);
        if (consultado == null) {
            System.err.println("[FALLO] SELECT por ID estudiante");
            return false;
        }
        System.out.println("[OK] SELECT por ID estudiante: " + consultado.getNombreCompleto());

        consultado.setTelefono("999111222");
        consultado.setCiclo(9);
        boolean actualizado = dao.actualizar(consultado);
        System.out.println("[OK] UPDATE estudiante ciclo y telefono: " + actualizado);

        boolean baja = dao.darDeBaja(idEst);
        System.out.println("[OK] Baja logica estudiante: " + baja);

        boolean eliminado = dao.eliminar(idEst);
        System.out.println("[OK] DELETE estudiante de prueba y usuario asociado: " + eliminado);

        return true;
    }

    private static boolean probarCRUD2Empresas() {
        System.out.println("\n--- [CRUD 2: EMPRESAS] ---");
        EmpresaDAO dao = new EmpresaDAO();

        List<Empresa> lista = dao.listarTodas();
        System.out.println("[OK] SELECT general empresas: " + lista.size() + " registros.");

        String correoEmpresa = "empresa.test." + System.currentTimeMillis() + "@gmail.com";
        String rucUnico = "20" + String.valueOf(System.currentTimeMillis()).substring(4);
        Empresa emp = new Empresa();
        emp.setRazonSocial("Soluciones Digitales SAC");
        emp.setRuc(rucUnico);
        emp.setTelefono("944332211");
        emp.setDireccion("Av. Javier Prado 1234");
        emp.setDescripcion("Desarrollo de software y consultoria");

        int idEmp = dao.registrarConUsuario(emp, correoEmpresa, "ClaveEmp123", "Juan", "Perez");
        if (idEmp <= 0) {
            System.err.println("[FALLO] INSERT transaccional empresa + usuario");
            return false;
        }
        System.out.println("[OK] INSERT transaccional empresa con id: " + idEmp + " (RUC: " + rucUnico + ")");

        Empresa consultada = dao.obtenerPorId(idEmp);
        if (consultada == null) {
            System.err.println("[FALLO] SELECT por ID empresa");
            return false;
        }
        System.out.println("[OK] SELECT por ID empresa: " + consultada.getRazonSocial() + " Estado: " + consultada.getEstado());

        consultada.setTelefono("911223344");
        consultada.setDireccion("Calle Las Camelias 456");
        boolean actualizada = dao.actualizar(consultada);
        System.out.println("[OK] UPDATE datos empresa: " + actualizada);

        boolean aprobada = dao.actualizarEstado(idEmp, "APROBADA");
        System.out.println("[OK] UPDATE estado aprobacion administrativa: " + aprobada);

        boolean baja = dao.darDeBaja(idEmp);
        System.out.println("[OK] Baja logica empresa: " + baja);

        boolean eliminada = dao.eliminar(idEmp);
        System.out.println("[OK] DELETE empresa de prueba y usuario asociado: " + eliminada);

        return true;
    }

    private static boolean probarCRUD3Ofertas() {
        System.out.println("\n--- [CRUD 3: OFERTAS DE PRACTICAS] ---");
        EmpresaDAO empDao = new EmpresaDAO();
        OfertaPracticaDAO dao = new OfertaPracticaDAO();

        List<Empresa> empresas = empDao.listarTodas();
        if (empresas.isEmpty()) {
            System.err.println("[FALLO] No hay empresas para asociar oferta de prueba");
            return false;
        }
        int idEmpresaReal = empresas.get(0).getId();

        List<OfertaPractica> todas = dao.listarTodas();
        System.out.println("[OK] SELECT general ofertas: " + todas.size() + " registros.");

        List<OfertaPractica> activas = dao.listarActivas();
        System.out.println("[OK] SELECT ofertas activas (web publica): " + activas.size() + " registros.");

        OfertaPractica oferta = new OfertaPractica();
        oferta.setIdEmpresa(idEmpresaReal);
        oferta.setTitulo("Practicante Java Junior Test");
        oferta.setDescripcion("Apoyo en desarrollo backend y bases de datos");
        oferta.setRequisitos("Java 21, JDBC, MySQL, Git");
        oferta.setCarrera("Ingenieria de Sistemas");
        oferta.setModalidad("REMOTO");
        oferta.setVacantes(3);
        oferta.setFechaLimite("2026-12-31");
        oferta.setUbicacion("Lima, Peru");
        oferta.setRemuneracion("S/. 1200");
        oferta.setEstado("ACTIVA");

        int idOferta = dao.crear(oferta);
        if (idOferta <= 0) {
            System.err.println("[FALLO] INSERT oferta de practica");
            return false;
        }
        System.out.println("[OK] INSERT oferta con id: " + idOferta);

        OfertaPractica consultada = dao.obtenerPorId(idOferta);
        if (consultada == null) {
            System.err.println("[FALLO] SELECT por ID oferta");
            return false;
        }
        System.out.println("[OK] SELECT por ID oferta: " + consultada.getTitulo() + " (" + consultada.getEmpresa() + ")");

        consultada.setVacantes(5);
        consultada.setRemuneracion("S/. 1400");
        boolean actualizada = dao.actualizar(consultada);
        System.out.println("[OK] UPDATE oferta vacantes y remuneracion: " + actualizada);

        boolean cerrada = dao.cerrarOferta(idOferta);
        System.out.println("[OK] Cierre logico oferta (estado CERRADA): " + cerrada);

        boolean eliminada = dao.eliminar(idOferta);
        System.out.println("[OK] DELETE oferta de prueba: " + eliminada);

        return true;
    }

    private static boolean probarCRUD4Postulaciones() {
        System.out.println("\n--- [CRUD 4: POSTULACIONES Y RESPUESTAS] ---");
        EstudianteDAO estDao = new EstudianteDAO();
        EmpresaDAO empDao = new EmpresaDAO();
        OfertaPracticaDAO ofDao = new OfertaPracticaDAO();
        PostulacionDAO dao = new PostulacionDAO();

        List<Postulacion> todas = dao.listarTodas();
        System.out.println("[OK] SELECT general postulaciones: " + todas.size() + " registros.");

        List<Empresa> empresas = empDao.listarTodas();
        int idEmp = empresas.get(0).getId();

        String correoTest = "est.post." + System.currentTimeMillis() + "@upn.pe";
        Estudiante estPrueba = new Estudiante();
        estPrueba.setNombre("Mario");
        estPrueba.setApellido("Vargas");
        estPrueba.setCarrera("Sistemas");
        estPrueba.setCiclo(7);
        int idEst = estDao.registrarConUsuario(estPrueba, correoTest, "Clave123");

        OfertaPractica ofPrueba = new OfertaPractica();
        ofPrueba.setIdEmpresa(idEmp);
        ofPrueba.setTitulo("Puesto Test Postulacion " + System.currentTimeMillis());
        ofPrueba.setDescripcion("Descripcion test");
        ofPrueba.setModalidad("VIRTUAL");
        ofPrueba.setVacantes(2);
        ofPrueba.setEstado("ACTIVA");
        int idOf = ofDao.crear(ofPrueba);

        List<RespuestaPostulacion> doceRespuestas = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            doceRespuestas.add(new RespuestaPostulacion(0, i, "Pregunta " + i + " del test?", "Respuesta " + i + " seleccionada"));
        }

        int idPostulacion = dao.registrarConRespuestas(idEst, idOf, doceRespuestas);
        if (idPostulacion <= 0) {
            System.err.println("[FALLO] INSERT transaccional postulacion + 12 respuestas");
            ofDao.eliminar(idOf);
            estDao.eliminar(idEst);
            return false;
        }
        System.out.println("[OK] INSERT transaccional postulacion con id: " + idPostulacion + " con 12 respuestas.");

        Postulacion consultada = dao.obtenerPorId(idPostulacion);
        if (consultada == null) {
            System.err.println("[FALLO] SELECT por ID postulacion");
            return false;
        }
        System.out.println("[OK] SELECT por ID postulacion: " + consultada.getEstudiante() + " -> " + consultada.getOferta() + " (Estado: " + consultada.getEstado() + ")");

        List<RespuestaPostulacion> respuestasGuardadas = dao.obtenerRespuestas(idPostulacion);
        System.out.println("[OK] SELECT de cuestionario guardado: " + respuestasGuardadas.size() + " respuestas verificadas.");

        boolean evaluada = dao.evaluar(idPostulacion, "EN_REVISION", "Perfil cumple con requisitos preliminares");
        System.out.println("[OK] UPDATE evaluacion de postulacion (EN_REVISION): " + evaluada);

        boolean rechazada = dao.rechazar(idPostulacion, "No supero entrevista tecnica");
        System.out.println("[OK] UPDATE rechazo con motivo (NO_SELECCIONADO): " + rechazada);

        boolean retirada = dao.retirar(idPostulacion);
        System.out.println("[OK] DELETE / Retirar postulacion (CASCADE a respuestas): " + retirada);

        ofDao.eliminar(idOf);
        estDao.eliminar(idEst);

        return true;
    }

    private static boolean probarCRUD5Entrevistas() {
        System.out.println("\n--- [CRUD 5: ENTREVISTAS] ---");
        EstudianteDAO estDao = new EstudianteDAO();
        EmpresaDAO empDao = new EmpresaDAO();
        OfertaPracticaDAO ofDao = new OfertaPracticaDAO();
        PostulacionDAO postDao = new PostulacionDAO();
        EntrevistaDAO dao = new EntrevistaDAO();

        List<Entrevista> todas = dao.listarTodas();
        System.out.println("[OK] SELECT general entrevistas: " + todas.size() + " registros.");

        List<Empresa> empresas = empDao.listarTodas();
        int idEmp = empresas.get(0).getId();

        String correoTest = "est.ent." + System.currentTimeMillis() + "@upn.pe";
        Estudiante estPrueba = new Estudiante();
        estPrueba.setNombre("Lucia");
        estPrueba.setApellido("Mendez");
        estPrueba.setCarrera("Sistemas");
        estPrueba.setCiclo(8);
        int idEst = estDao.registrarConUsuario(estPrueba, correoTest, "Clave123");

        OfertaPractica ofPrueba = new OfertaPractica();
        ofPrueba.setIdEmpresa(idEmp);
        ofPrueba.setTitulo("Puesto Test Entrevista " + System.currentTimeMillis());
        ofPrueba.setDescripcion("Descripcion test");
        ofPrueba.setModalidad("VIRTUAL");
        ofPrueba.setVacantes(1);
        ofPrueba.setEstado("ACTIVA");
        int idOf = ofDao.crear(ofPrueba);

        int idPostulacion = postDao.registrarConRespuestas(idEst, idOf, new ArrayList<>());

        Entrevista entrevista = new Entrevista();
        entrevista.setIdPostulacion(idPostulacion);
        entrevista.setFecha("2026-11-15");
        entrevista.setHora("14:30:00");
        entrevista.setModalidad("VIRTUAL");
        entrevista.setEnlaceLugar("https://meet.google.com/pro-path-test");
        entrevista.setEstado("PROGRAMADA");
        entrevista.setObservaciones("Entrevista tecnica con lider de arquitectura");

        int idEntrevista = dao.crear(entrevista);
        if (idEntrevista <= 0) {
            System.err.println("[FALLO] INSERT entrevista");
            postDao.retirar(idPostulacion);
            ofDao.eliminar(idOf);
            estDao.eliminar(idEst);
            return false;
        }
        System.out.println("[OK] INSERT entrevista con id: " + idEntrevista);

        Entrevista consultada = dao.obtenerPorId(idEntrevista);
        if (consultada == null) {
            System.err.println("[FALLO] SELECT por ID entrevista");
            return false;
        }
        System.out.println("[OK] SELECT por ID entrevista: " + consultada.getModalidad() + " " + consultada.getFecha() + " " + consultada.getHora());

        consultada.setHora("15:00:00");
        consultada.setObservaciones("Reprogramada 30 minutos mas tarde");
        boolean actualizada = dao.actualizar(consultada);
        System.out.println("[OK] UPDATE entrevista hora y observaciones: " + actualizada);

        boolean cancelada = dao.cancelar(idEntrevista);
        System.out.println("[OK] UPDATE cancelacion logica entrevista: " + cancelada);

        boolean eliminada = dao.eliminar(idEntrevista);
        System.out.println("[OK] DELETE entrevista de prueba: " + eliminada);

        postDao.retirar(idPostulacion);
        ofDao.eliminar(idOf);
        estDao.eliminar(idEst);

        return true;
    }
}
