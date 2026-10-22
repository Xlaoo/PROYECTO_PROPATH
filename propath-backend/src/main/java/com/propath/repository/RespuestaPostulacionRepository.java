
package com.propath.repository;

import com.propath.entity.RespuestaPostulacion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RespuestaPostulacionRepository
        extends JpaRepository<RespuestaPostulacion, Integer> {

    List<RespuestaPostulacion> findByPostulacionId(Integer idPostulacion);

    List<RespuestaPostulacion> findByPostulacionIdOrderByNumeroPreguntaAsc(
            Integer idPostulacion);
}