package com.propath.dao;

import com.propath.modelo.Empresa;
import com.propath.modelo.Estudiante;
import com.propath.modelo.OfertaPractica;
import com.propath.modelo.Postulacion;
import java.util.ArrayList;
import java.util.List;

public class DatosSimuladosDAO {

    public static List<Estudiante> obtenerEstudiantes() {
        List<Estudiante> lista = new ArrayList<>();
        lista.add(new Estudiante(1, "Ana", "Torres", "ana@gmail.com", "UPN", "Ingeniería de Sistemas", 8, "Activo"));
        lista.add(new Estudiante(2, "Luis", "Mendoza", "luis@gmail.com", "UPC", "Ingeniería Industrial", 7, "Activo"));
        lista.add(new Estudiante(3, "Carla", "Ruiz", "carla@gmail.com", "UTP", "Administración", 9, "Bloqueado"));
        return lista;
    }

    public static List<Empresa> obtenerEmpresas() {
        List<Empresa> lista = new ArrayList<>();
        lista.add(new Empresa(1, "Tech Solutions SAC", "20123456789", "contacto@techsolutions.com", "987654321",
                "Aprobada"));
        lista.add(new Empresa(2, "Innovate Perú SAC", "20987654321", "rrhh@innovate.com", "912345678", "Pendiente"));
        return lista;
    }

    public static List<OfertaPractica> obtenerOfertas() {
        List<OfertaPractica> lista = new ArrayList<>();
        lista.add(new OfertaPractica(1, "Practicante de Desarrollo Web", "Tech Solutions SAC", "Ingeniería de Sistemas",
                "Híbrida", 2, "28/09/2026", "Activa"));
        lista.add(new OfertaPractica(2, "Practicante de Recursos Humanos", "Innovate Perú SAC", "Administración",
                "Presencial", 1, "27/09/2026", "Pendiente"));
        return lista;
    }

    public static List<Postulacion> obtenerPostulaciones() {
        List<Postulacion> lista = new ArrayList<>();
        lista.add(new Postulacion(1, "Ana Torres", "Practicante de Desarrollo Web", "Tech Solutions SAC", "29/09/2026",
                "En revisión"));
        lista.add(new Postulacion(2, "Luis Mendoza", "Practicante de Soporte TI", "Digital Corp", "28/09/2026",
                "Postulado"));
        return lista;
    }
}
