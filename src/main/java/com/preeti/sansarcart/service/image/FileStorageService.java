package com.preeti.sansarcart.service.image;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;

public interface FileStorageService {
    void saveFile(String directory, String filename, MultipartFile file) throws IOException;
    Path getFilePath(String directory, String filename);
    Path getBaseStorageLocation();
}
