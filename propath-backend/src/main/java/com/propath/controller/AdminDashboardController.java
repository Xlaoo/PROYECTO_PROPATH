package com.propath.controller;

import com.propath.entity.Empresa;
import com.propath.entity.OfertaPractica;
import com.propath.entity.Postulacion;
import com.propath.entity.Usuario;
import com.propath.repository.EmpresaRepository;
import com.propath.repository.EstudianteRepository;
import com.propath.repository.OfertaPracticaRepository;
import com.propath.repository.PostulacionRepository;
import com.propath.repository.UsuarioRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/estadisticas")
public class AdminDashboardController {

    private final EstudianteRepository estudianteRepository;
    private final EmpresaRepository empresaRepository;
    private final OfertaPracticaRepository ofertaPracticaRepository;
    private final PostulacionRepository postulacionRepository;
    private final UsuarioRepository usuarioRepository;

    public AdminDashboardController(EstudianteRepository estudianteRepository,
                                    EmpresaRepository empresaRepository,
                                    OfertaPracticaRepository ofertaPracticaRepository,
                                    PostulacionRepository postulacionRepository,
                                    UsuarioRepository usuarioRepository) {
        this.estudianteRepository = estudianteRepository;
        this.empresaRepository = empresaRepository;
        this.ofertaPracticaRepository = ofertaPracticaRepository;
        this.postulacionRepository = postulacionRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> obtenerEstadisticas() {
        Map<String, Object> stats = new HashMap<>();

        long totalEstudiantes = estudianteRepository.count();
        LocalDateTime inicioSemana = LocalDate.now()
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .atStartOfDay();
        long estudiantesEstaSemana = usuarioRepository.countByRolAndFechaRegistroGreaterThanEqual(Usuario.Rol.ESTUDIANTE, inicioSemana);

        long totalEmpresas = empresaRepository.count();
        long empresasAprobadas = empresaRepository.countByEstado(Empresa.Estado.APROBADA);
        long empresasPendientes = empresaRepository.countByEstado(Empresa.Estado.PENDIENTE);

        long totalOfertas = ofertaPracticaRepository.count();
        long ofertasActivas = ofertaPracticaRepository.countByEstado(OfertaPractica.Estado.ACTIVA);
        long ofertasPendientes = ofertaPracticaRepository.countByEstado(OfertaPractica.Estado.PENDIENTE);

        long totalPostulaciones = postulacionRepository.count();
        long postulacionesEnRevision = postulacionRepository.countByEstado(Postulacion.Estado.EN_REVISION);
        long postulacionesSeleccionadas = postulacionRepository.countByEstado(Postulacion.Estado.SELECCIONADO);

        stats.put("totalEstudiantes", totalEstudiantes);
        stats.put("estudiantesEstaSemana", estudiantesEstaSemana);
        stats.put("totalEmpresas", totalEmpresas);
        stats.put("empresasAprobadas", empresasAprobadas);
        stats.put("empresasPendientes", empresasPendientes);
        stats.put("totalOfertas", totalOfertas);
        stats.put("ofertasActivas", ofertasActivas);
        stats.put("ofertasPendientes", ofertasPendientes);
        stats.put("totalPostulaciones", totalPostulaciones);
        stats.put("postulacionesEnRevision", postulacionesEnRevision);
        stats.put("postulacionesSeleccionadas", postulacionesSeleccionadas);

        return ResponseEntity.ok(stats);
    }
}
