package com.propath.repository;

import com.propath.entity.Entrevista;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EntrevistaRepository extends JpaRepository<Entrevista, Integer> {
    List<Entrevista> findByPostulacionEstudianteId(Integer idEstudiante);

    List<Entrevista> findByPostulacionOfertaEmpresaId(Integer idEmpresa);

    boolean existsByPostulacionId(Integer idPostulacion);
}