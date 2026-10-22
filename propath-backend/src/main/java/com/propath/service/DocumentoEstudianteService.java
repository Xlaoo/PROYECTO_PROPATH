package com.propath.service;

import com.propath.entity.DocumentoEstudiante;
import com.propath.entity.Estudiante;
import com.propath.entity.Usuario;
import com.propath.repository.DocumentoEstudianteRepository;
import com.propath.repository.EstudianteRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class DocumentoEstudianteService {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
    private final Path storageDirectory;
    private final DocumentoEstudianteRepository documentoRepository;
    private final EstudianteRepository estudianteRepository;

    public DocumentoEstudianteService(DocumentoEstudianteRepository documentoRepository,
                                     EstudianteRepository estudianteRepository) {
        this.documentoRepository = documentoRepository;
        this.estudianteRepository = estudianteRepository;

        String customPath = System.getProperty("PROPATH_STORAGE_DIR", "D:\\PROYECTO_PROPATH\\storage\\documentos");
        this.storageDirectory = Paths.get(customPath).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.storageDirectory);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo inicializar el directorio de almacenamiento: " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public Optional<DocumentoEstudiante> buscarCvPorEstudiante(Integer idEstudiante) {
        if (idEstudiante == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El ID de estudiante no puede ser nulo.");
        }
        return documentoRepository.findByEstudianteIdAndTipoDocumento(idEstudiante, "CV");
    }

    @Transactional
    public DocumentoEstudiante guardarCv(Integer idEstudiante, String nombreOriginal, byte[] contenidoPdf, String contentType) {
        if (idEstudiante == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El ID de estudiante no puede ser nulo.");
        }
        if (contenidoPdf == null || contenidoPdf.length == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El archivo PDF no puede estar vacío.");
        }
        if (contenidoPdf.length > MAX_FILE_SIZE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El archivo supera el tamaño máximo permitido de 5 MB.");
        }

        if (contenidoPdf.length < 4 ||
            contenidoPdf[0] != 0x25 || contenidoPdf[1] != 0x50 ||
            contenidoPdf[2] != 0x44 || contenidoPdf[3] != 0x46) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El archivo seleccionado no es un documento PDF válido.");
        }

        Estudiante estudiante = estudianteRepository.findById(idEstudiante)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Estudiante no encontrado con ID: " + idEstudiante));

        if (estudiante.getUsuario() != null && estudiante.getUsuario().getEstado() != Usuario.Estado.ACTIVO) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "La cuenta del estudiante no se encuentra activa.");
        }

        String nombreLimpio = nombreOriginal != null ? Paths.get(nombreOriginal).getFileName().toString() : "CV_Estudiante.pdf";
        if (!nombreLimpio.toLowerCase().endsWith(".pdf")) {
            nombreLimpio += ".pdf";
        }

        String safeFileName = "cv_estudiante_" + idEstudiante + "_" + UUID.randomUUID().toString().replace("-", "").substring(0, 10) + ".pdf";
        Path targetPath = this.storageDirectory.resolve(safeFileName).normalize();

        try {
            Files.write(targetPath, contenidoPdf);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al escribir el archivo en almacenamiento persistente: " + e.getMessage());
        }

        Optional<DocumentoEstudiante> docExistente = documentoRepository.findByEstudianteIdAndTipoDocumento(idEstudiante, "CV");
        String archivoAnteriorBorrar = null;

        DocumentoEstudiante doc;
        if (docExistente.isPresent()) {
            doc = docExistente.get();
            archivoAnteriorBorrar = doc.getArchivoRuta();
        } else {
            doc = new DocumentoEstudiante();
            doc.setEstudiante(estudiante);
            doc.setTipoDocumento("CV");
        }

        doc.setNombreArchivo(nombreLimpio);
        doc.setArchivoRuta(safeFileName);
        doc.setTamanioBytes((long) contenidoPdf.length);
        doc.setContentType("application/pdf");
        doc.setFechaSubida(LocalDateTime.now());

        DocumentoEstudiante guardado = documentoRepository.save(doc);

        estudiante.setCv(nombreLimpio);
        estudianteRepository.save(estudiante);

        if (archivoAnteriorBorrar != null && !archivoAnteriorBorrar.equals(safeFileName)) {
            try {
                Path oldFile = this.storageDirectory.resolve(archivoAnteriorBorrar).normalize();
                Files.deleteIfExists(oldFile);
            } catch (IOException ignored) {}
        }

        return guardado;
    }

    @Transactional(readOnly = true)
    public File obtenerArchivoCv(Integer idEstudiante) {
        if (idEstudiante == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El ID de estudiante no puede ser nulo.");
        }
        Estudiante estudiante = estudianteRepository.findById(idEstudiante)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Estudiante no encontrado con ID: " + idEstudiante));

        if (estudiante.getUsuario() != null && estudiante.getUsuario().getEstado() != Usuario.Estado.ACTIVO) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "La cuenta del estudiante no se encuentra activa.");
        }

        DocumentoEstudiante doc = documentoRepository.findByEstudianteIdAndTipoDocumento(idEstudiante, "CV")
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El estudiante no tiene un currículum vitae registrado."));

        Path filePath = this.storageDirectory.resolve(doc.getArchivoRuta()).normalize();
        File file = filePath.toFile();
        if (!file.exists() || !file.isFile()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El archivo PDF no se encuentra en el almacenamiento.");
        }
        return file;
    }

    @Transactional
    public void eliminarCv(Integer idEstudiante) {
        if (idEstudiante == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El ID de estudiante no puede ser nulo.");
        }
        Estudiante estudiante = estudianteRepository.findById(idEstudiante)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Estudiante no encontrado con ID: " + idEstudiante));

        DocumentoEstudiante doc = documentoRepository.findByEstudianteIdAndTipoDocumento(idEstudiante, "CV")
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El estudiante no tiene un currículum para eliminar."));

        String archivoFisico = doc.getArchivoRuta();
        documentoRepository.delete(doc);

        estudiante.setCv(null);
        estudianteRepository.save(estudiante);

        if (archivoFisico != null) {
            try {
                Path fileToDelete = this.storageDirectory.resolve(archivoFisico).normalize();
                Files.deleteIfExists(fileToDelete);
            } catch (IOException ignored) {}
        }
    }
}
