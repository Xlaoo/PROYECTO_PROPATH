package com.propath.repository;

import com.propath.entity.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, Integer> {
    java.util.Optional<Empresa> findByRuc(String ruc);
    java.util.Optional<Empresa> findByUsuarioId(Integer idUsuario);
    boolean existsByRuc(String ruc);
    long countByEstado(Empresa.Estado estado);
}
