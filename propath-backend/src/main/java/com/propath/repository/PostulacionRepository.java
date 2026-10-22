package com.propath.repository;

import com.propath.entity.Postulacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostulacionRepository
        extends JpaRepository<Postulacion, Integer> {

    List<Postulacion> findByEstudianteId(Integer idEstudiante);

    List<Postulacion> findByOfertaId(Integer idOferta);

    List<Postulacion> findByOfertaEmpresaId(Integer idEmpresa);

    boolean existsByEstudianteIdAndOfertaId(
            Integer idEstudiante,
            Integer idOferta);

    long countByEstado(Postulacion.Estado estado);

    long countByOfertaIdAndEstadoIn(
            Integer idOferta,
            List<Postulacion.Estado> estados);
}