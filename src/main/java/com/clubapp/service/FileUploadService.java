package com.clubapp.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class FileUploadService {

    private static final String BASE_UPLOAD_DIR = "src/main/resources/static/uploads/";

    public String uploadFile(MultipartFile file, String subDir) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        String filename = subDir + "-" + UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path uploadPath = Paths.get(BASE_UPLOAD_DIR + subDir + "/");
        
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        Files.copy(file.getInputStream(), uploadPath.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
        
        // Return relative path for storage in DB and serving
        return subDir + "/" + filename;
    }
}
