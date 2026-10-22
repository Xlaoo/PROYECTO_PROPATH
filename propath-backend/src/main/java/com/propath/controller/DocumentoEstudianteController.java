package com.propath.controller;

import com.propath.entity.DocumentoEstudiante;
import com.propath.service.DocumentoEstudianteService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/estudiantes/{idEstudiante}/documentos")
public class DocumentoEstudianteController {

    private final DocumentoEstudianteService documentoService;

    public DocumentoEstudianteController(DocumentoEstudianteService documentoService) {
        this.documentoService = documentoService;
    }

    @GetMapping("/cv")
    public ResponseEntity<DocumentoEstudiante> obtenerCv(@PathVariable("idEstudiante") Integer idEstudiante) {
        DocumentoEstudiante doc = documentoService.buscarCvPorEstudiante(idEstudiante)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontró un currículum vitae para este estudiante."));
        return ResponseEntity.ok(doc);
    }

    @PostMapping(value = "/cv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentoEstudiante> subirCv(
            @PathVariable("idEstudiante") Integer idEstudiante,
            @RequestParam("archivo") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe seleccionar un archivo PDF.");
        }
        try {
            byte[] bytes = file.getBytes();
            DocumentoEstudiante doc = documentoService.guardarCv(idEstudiante, file.getOriginalFilename(), bytes, file.getContentType());
            return ResponseEntity.status(HttpStatus.CREATED).body(doc);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al leer el archivo enviado: " + e.getMessage());
        }
    }

    @PutMapping(value = "/cv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentoEstudiante> reemplazarCv(
            @PathVariable("idEstudiante") Integer idEstudiante,
            @RequestParam("archivo") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe seleccionar un archivo PDF para el reemplazo.");
        }
        try {
            byte[] bytes = file.getBytes();
            DocumentoEstudiante doc = documentoService.guardarCv(idEstudiante, file.getOriginalFilename(), bytes, file.getContentType());
            return ResponseEntity.ok(doc);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al leer el archivo de reemplazo: " + e.getMessage());
        }
    }

    @GetMapping("/cv/descargar")
    public ResponseEntity<Resource> descargarCv(
            @PathVariable("idEstudiante") Integer idEstudiante,
            @RequestParam(value = "inline", defaultValue = "false") boolean inline) {
        DocumentoEstudiante doc = documentoService.buscarCvPorEstudiante(idEstudiante)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontró el currículum solicitado."));

        File file = documentoService.obtenerArchivoCv(idEstudiante);
        Resource resource = new FileSystemResource(file);

        String dispositionType = inline ? "inline" : "attachment";
        String encodedFilename = URLEncoder.encode(doc.getNombreArchivo(), StandardCharsets.UTF_8).replace("+", "%20");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.set(HttpHeaders.CONTENT_DISPOSITION, dispositionType + "; filename=\"" + doc.getNombreArchivo() + "\"; filename*=UTF-8''" + encodedFilename);
        headers.setContentLength(file.length());

        return ResponseEntity.ok()
                .headers(headers)
                .body(resource);
    }

    @DeleteMapping("/cv")
    public ResponseEntity<Void> eliminarCv(@PathVariable("idEstudiante") Integer idEstudiante) {
        documentoService.eliminarCv(idEstudiante);
        return ResponseEntity.noContent().build();
    }
}
