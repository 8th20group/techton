package com.techton.storage;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorage {

    String store(MultipartFile file);
}
