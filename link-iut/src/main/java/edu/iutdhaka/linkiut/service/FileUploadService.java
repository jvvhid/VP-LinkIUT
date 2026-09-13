package edu.iutdhaka.linkiut.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileUploadService {

    @Value("${file.upload-dir:uploads/}")
    private String uploadDir;

    public String storeFile(MultipartFile file) {
        if (file.isEmpty()) {
            return null;
        }

        try {
            // Ensure directory exists
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate unique file name
            String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
            String fileExtension = "";
            int dotIndex = originalFilename.lastIndexOf('.');
            if (dotIndex > 0) {
                fileExtension = originalFilename.substring(dotIndex).toLowerCase();
            }

            java.util.List<String> allowedExtensions = java.util.Arrays.asList(
                    ".jpg", ".jpeg", ".png", ".gif", ".webp",
                    ".mp4", ".mov", ".webm",
                    ".mp3", ".wav", ".ogg",
                    ".pdf", ".doc", ".docx"
            );

            if (!allowedExtensions.contains(fileExtension)) {
                throw new RuntimeException("File type not allowed");
            }

            String fileName = UUID.randomUUID().toString() + fileExtension;

            // Copy file to destination
            Path targetLocation = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return "/uploads/" + fileName;

        } catch (IOException ex) {
            throw new RuntimeException("Could not store file. Please try again!", ex);
        }
    }
}
