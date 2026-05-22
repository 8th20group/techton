package com.techton.storage;

import com.techton.global.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MissionImageController {

    private final MissionImageRepository missionImageRepository;

    @GetMapping("/images/{id}")
    public ResponseEntity<byte[]> getImage(@PathVariable Long id) {
        MissionImage image = missionImageRepository.findById(id)
                .orElseThrow(() -> new BusinessException("이미지를 찾을 수 없습니다."));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(image.getContentType()));
        headers.setContentDisposition(ContentDisposition.inline()
                .filename(image.getOriginalFilename())
                .build());
        return ResponseEntity.ok().headers(headers).body(image.getData());
    }
}
