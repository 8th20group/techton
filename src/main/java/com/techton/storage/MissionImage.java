package com.techton.storage;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MissionImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    @Column(nullable = false)
    private byte[] data;

    @Column(nullable = false)
    private String contentType;

    @Column(nullable = false)
    private String originalFilename;

    public MissionImage(byte[] data, String contentType, String originalFilename) {
        this.data = data;
        this.contentType = contentType;
        this.originalFilename = originalFilename;
    }
}
