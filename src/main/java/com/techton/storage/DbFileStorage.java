package com.techton.storage;

import com.techton.global.BusinessException;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
@RequiredArgsConstructor
public class DbFileStorage implements FileStorage {

    private final MissionImageRepository missionImageRepository;

    @Override
    public String store(MultipartFile file) {
        try {
            String contentType = file.getContentType() != null ? file.getContentType() : "application/octet-stream";
            String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "mission";
            MissionImage image = missionImageRepository.save(new MissionImage(file.getBytes(), contentType, originalFilename));
            return "/images/" + image.getId();
        } catch (IOException e) {
            throw new BusinessException("파일 저장에 실패했습니다.");
        }
    }
}
