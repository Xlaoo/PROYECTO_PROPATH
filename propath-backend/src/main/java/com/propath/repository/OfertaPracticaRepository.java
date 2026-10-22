package com.propath.repository;

import com.propath.entity.OfertaPractica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OfertaPracticaRepository extends JpaRepository<OfertaPractica, Integer> {
    java.util.List<OfertaPractica> findByEmpresaId(Integer idEmpresa);
    java.util.List<OfertaPractica> findByEstado(OfertaPractica.Estado estado);
    long countByEstado(OfertaPractica.Estado estado);
}
