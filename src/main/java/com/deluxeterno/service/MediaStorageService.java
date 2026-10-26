package com.deluxeterno.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.PathResource;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import javax.imageio.ImageIO;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class MediaStorageService {
    private final Path root;
    public MediaStorageService(@Value("${app.media.directory:./uploads}") String directory) { root=Path.of(directory).toAbsolutePath().normalize(); }

    public String store(MultipartFile file) {
        if (file.isEmpty() || file.getSize()>5L*1024*1024) throw new IllegalArgumentException("Selecciona una fotografía JPG o PNG de hasta 5 MB.");
        try (var input=ImageIO.createImageInputStream(file.getInputStream())) {
            var readers=ImageIO.getImageReaders(input);
            if (!readers.hasNext()) throw new IllegalArgumentException("El archivo no es una imagen JPG o PNG válida.");
            var reader=readers.next();
            try {
                reader.setInput(input,true,true);
                String format=reader.getFormatName().toLowerCase(java.util.Locale.ROOT);
                if (!format.equals("jpeg") && !format.equals("png")) throw new IllegalArgumentException("Por ahora se admiten fotografías JPG y PNG.");
                int width=reader.getWidth(0),height=reader.getHeight(0);
                if (width<64 || height<64 || (long)width*height>24000000) throw new IllegalArgumentException("La imagen debe tener al menos 64 píxeles por lado y no superar 24 megapíxeles.");
                BufferedImage original=reader.read(0);
                double scale=Math.min(1d,1600d/Math.max(width,height));
                boolean png=format.equals("png");
                var image=new BufferedImage(Math.max(1,(int)(width*scale)),Math.max(1,(int)(height*scale)),png?BufferedImage.TYPE_INT_ARGB:BufferedImage.TYPE_INT_RGB);
                var graphics=image.createGraphics();
                try { graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BICUBIC); graphics.drawImage(original,0,0,image.getWidth(),image.getHeight(),null); }
                finally { graphics.dispose(); }
                Files.createDirectories(root);
                String filename=UUID.randomUUID()+(png?".png":".jpg");
                Path target=root.resolve(filename);
                // Re-encoding discards supplied metadata and content beyond the decoded image.
                if (!ImageIO.write(image,png?"png":"jpeg",target.toFile())) throw new IOException("Formato no disponible");
                return "/media/"+filename;
            } finally { reader.dispose(); }
        } catch (IOException exception) { throw new IllegalArgumentException("No pudimos procesar o guardar la fotografía. Comprueba el archivo y el almacenamiento."); }
    }

    public Resource load(String filename) {
        if (!filename.matches("[a-f0-9-]{36}\\.(jpg|png)")) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        Path path=root.resolve(filename).normalize();
        if (!path.startsWith(root) || !Files.isRegularFile(path)) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return new PathResource(path);
    }
}
