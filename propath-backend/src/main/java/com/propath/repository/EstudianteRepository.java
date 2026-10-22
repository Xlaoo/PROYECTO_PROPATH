package com.propath.repository;

import com.propath.entity.Estudiante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EstudianteRepository extends JpaRepository<Estudiante, Integer> {
    java.util.Optional<Estudiante> findByUsuarioId(Integer idUsuario);
}
