package lk.ceylontravel.controller;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Principal;
import java.util.Map;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import lk.ceylontravel.config.UploadPolicy;
import lk.ceylontravel.model.Access;

@RestController
public class UploadController {

    private final Access access;
    private final Path directory;

    public UploadController(
            Access access,
            @Value("${app.upload-dir}") String path) {

        this.access = access;

        this.directory = Path.of(path)
                .toAbsolutePath()
                .normalize();
    }

    @PostMapping("/api/uploads")
    Object upload(
            Principal p,
            @RequestParam MultipartFile file) throws Exception {

        // Check whether the user is authorized
        access.id(p);

        // Get the Singleton UploadPolicy instance
        UploadPolicy policy = UploadPolicy.getInstance();

        // Validate file type and file size
        if (file.isEmpty()
                || !policy.isAllowedUpload(
                        file.getContentType(),
                        file.getSize())) {

            throw new ResponseStatusException(
                    BAD_REQUEST,
                    "Select a JPG or PNG image under 5 MB");
        }

        /*
         * Check whether the uploaded file
         * is actually a valid image.
         */
        try (var stream = ImageIO.createImageInputStream(
                file.getInputStream())) {

            var readers = ImageIO.getImageReaders(stream);

            if (!readers.hasNext()) {
                throw new ResponseStatusException(
                        BAD_REQUEST,
                        "This file is not a valid image");
            }

            var reader = readers.next();

            try {
                reader.setInput(stream);

                // Validate image dimensions
                if (!policy.isValidPixelCount(
                        reader.getWidth(0),
                        reader.getHeight(0))) {

                    throw new ResponseStatusException(
                            BAD_REQUEST,
                            "Image must be under 16 megapixels");
                }

                // Read image
                var image = reader.read(0);

                // Create upload directory if necessary
                Files.createDirectories(directory);

                // Generate unique file name
                String name =
                        UUID.randomUUID() + ".png";

                // Save image as PNG
                ImageIO.write(
                        image,
                        "png",
                        directory.resolve(name).toFile());

                // Return uploaded image URL
                return Map.of(
                        "url",
                        "/media/" + name);

            } finally {
                reader.dispose();
            }
        }
    }

    /*
     * Serve uploaded files through /media/**
     */
    @Configuration
    static class Media implements WebMvcConfigurer {

        @Value("${app.upload-dir}")
        private String path;

        @Override
        public void addResourceHandlers(
                ResourceHandlerRegistry registry) {

            String uri = Path.of(path)
                    .toAbsolutePath()
                    .normalize()
                    .toUri()
                    .toString();

            if (!uri.endsWith("/")) {
                uri += "/";
            }

            registry.addResourceHandler("/media/**")
                    .addResourceLocations(uri);
        }
    }
}