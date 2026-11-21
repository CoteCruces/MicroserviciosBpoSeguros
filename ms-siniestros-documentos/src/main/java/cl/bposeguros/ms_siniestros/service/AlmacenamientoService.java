package cl.bposeguros.ms_siniestros.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;


@Service
public class AlmacenamientoService {

    private final Path carpeta;

    public AlmacenamientoService(@Value("${app.documentos.carpeta}") String carpeta) {
        this.carpeta = Path.of(carpeta).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.carpeta);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo crear la carpeta de documentos: " + this.carpeta, e);
        }
    }

    
    public String guardar(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se recibió ningún archivo");
        }
        String tipo = archivo.getContentType();
        boolean valido = tipo != null && (tipo.startsWith("image/") || tipo.equals("application/pdf"));
        if (!valido) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El archivo debe ser una imagen o un PDF");
        }
        String nombre = UUID.randomUUID() + extensionDe(tipo);
        try {
            Files.copy(archivo.getInputStream(), carpeta.resolve(nombre), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "No se pudo guardar el archivo");
        }
        return nombre;
    }

    public byte[] leer(String nombre) {
        try {
            return Files.readAllBytes(carpeta.resolve(nombre).normalize());
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El archivo no existe en el servidor");
        }
    }

    private String extensionDe(String tipo) {
        return switch (tipo) {
            case "application/pdf" -> ".pdf";
            case "image/png" -> ".png";
            default -> ".jpg";
        };
    }
}