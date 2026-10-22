package com.propath.repository;

import com.propath.entity.DocumentoEstudiante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentoEstudianteRepository extends JpaRepository<DocumentoEstudiante, Integer> {
    List<DocumentoEstudiante> findByEstudianteId(Integer idEstudiante);
    Optional<DocumentoEstudiante> findByEstudianteIdAndTipoDocumento(Integer idEstudiante, String tipoDocumento);
}
