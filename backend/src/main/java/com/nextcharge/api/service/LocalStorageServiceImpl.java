package com.nextcharge.api.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class LocalStorageServiceImpl implements StorageService {

    @Value("${app.storage.local-dir:uploads}")
    private String uploadDir;

    @Override
    public String store(MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new RuntimeException("Failed to store empty file.");
            }
            Path root = Paths.get(uploadDir).toAbsolutePath().normalize();
            if (!Files.exists(root)) {
                Files.createDirectories(root);
            }

            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String filename = UUID.randomUUID().toString() + extension;
            Path destination = root.resolve(filename);

            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
            return "/uploads/" + filename;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }
    }
}
