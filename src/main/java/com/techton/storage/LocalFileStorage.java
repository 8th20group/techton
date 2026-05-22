package com.techton.storage;

import com.techton.global.BusinessException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class LocalFileStorage implements FileStorage {

    private static final Path ROOT_PATH = Path.of("uploads", "missions");

    @Override
    public String store(MultipartFile file) {
        try {
            Files.createDirectories(ROOT_PATH);
            String originalFilename = file.getOriginalFilename();
            String extension = extractExtension(originalFilename);
            Path storedPath = ROOT_PATH.resolve(UUID.randomUUID() + extension);
            file.transferTo(storedPath);
            return storedPath.toString();
        } catch (IOException exception) {
            throw new BusinessException("파일 저장에 실패했습니다.");
        }
    }

    private String extractExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }
}
