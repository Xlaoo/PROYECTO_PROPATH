package com.propath.repository;

import com.propath.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    Optional<Usuario> findByCorreo(String correo);
    long countByRol(Usuario.Rol rol);
    long countByRolAndFechaRegistroGreaterThanEqual(Usuario.Rol rol, java.time.LocalDateTime fecha);
}
