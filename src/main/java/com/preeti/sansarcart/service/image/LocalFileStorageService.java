package com.preeti.sansarcart.service.image;

import com.preeti.sansarcart.exception.custom.ValidationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;

@Service
@Primary
public class LocalFileStorageService implements FileStorageService {

    private final Path BASE_DIR;

    public LocalFileStorageService(@Value("${app.file-storage.base-dir}") String baseDir) {
        this.BASE_DIR = Paths.get(baseDir).toAbsolutePath().normalize();
    }

    @Override
    public void saveFile(String directory, String filename, MultipartFile file) throws IOException {
        Path targetDir = BASE_DIR.resolve(directory);
        Files.createDirectories(targetDir);

        Path targetFile = targetDir.resolve(filename);
        file.transferTo(targetFile.toFile());
    }

    @Override
    public Path getFilePath(String directory, String filename) {
        Path targetPath = BASE_DIR.resolve(directory).resolve(filename).normalize();
        if (!targetPath.startsWith(BASE_DIR)) {
            throw new ValidationException("Invalid file path access attempt");
        }
        return targetPath;
    }


    @Override
    public Path getBaseStorageLocation(){
        return this.BASE_DIR;
    }
}

