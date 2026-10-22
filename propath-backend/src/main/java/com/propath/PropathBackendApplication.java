package com.propath;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;

@SpringBootApplication
public class PropathBackendApplication {

    private static final Logger log = LoggerFactory.getLogger(PropathBackendApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(PropathBackendApplication.class, args);
    }

    @Bean
    public CommandLineRunner verificarConexionBD(DataSource dataSource) {
        return args -> {
            log.info("----------------------------------------------------------------------");
            log.info("[PROPATH-BACKEND] Verificando conexion con MariaDB...");
            try (Connection conn = dataSource.getConnection()) {
                DatabaseMetaData meta = conn.getMetaData();
                log.info("[PROPATH-BACKEND] Conexion establecida exitosamente.");
                log.info("[PROPATH-BACKEND] Motor de BD: {} {}", meta.getDatabaseProductName(), meta.getDatabaseProductVersion());
                log.info("[PROPATH-BACKEND] Base de datos activa: {}", conn.getCatalog());
                log.info("[PROPATH-BACKEND] Driver JDBC: {} {}", meta.getDriverName(), meta.getDriverVersion());
                log.info("----------------------------------------------------------------------");
            } catch (Exception e) {
                log.error("[PROPATH-BACKEND] Error al conectar con MariaDB: {}", e.getMessage());
                log.info("----------------------------------------------------------------------");
            }
        };
    }
}
