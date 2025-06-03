package com.preeti.sansarcart.service.image;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;


import com.preeti.sansarcart.enums.EntityType;
import com.preeti.sansarcart.exception.custom.ValidationException;
import org.apache.tika.Tika;

import java.util.List;
import java.util.UUID;

import static com.preeti.sansarcart.common.I18n.i18n;

@Service
@RequiredArgsConstructor
public class ImageService {

    private static final List<String> SUPPORTED_IMAGE_TYPES = List.of("image/jpeg", "image/png", "image/bmp");
    private final Tika tika = new Tika();

    @Value("${app.base-url}")
    private String baseUrl;

    private final FileStorageService fileStorageService;

    public void saveEntityImage(EntityType entityType, UUID entityId, MultipartFile file, UUID parentId) {
        // this helps us know the mimetype based on the content not the user provided or the extension
        try {
            String mimeType = tika.detect(file.getInputStream());

            if (!SUPPORTED_IMAGE_TYPES.contains(mimeType)) {
                throw new ValidationException("Unsupported file type: " + mimeType);
            }

            String extension = getExtensionFromMime(mimeType);
            String directory = getDirectory(entityType, entityId, parentId);
            String filename = entityId + "." + extension;

            fileStorageService.saveFile(directory, filename, file);
        } catch (IOException e) {
            throw new ValidationException(i18n("exception.bad.request"));
        }
    }

    public void saveSecondaryImages(UUID productId, UUID variationId, List<MultipartFile> files) {
        String directory = "products/" + productId + "/variations/" + variationId + "/secondary_images";
        Path dirPath = fileStorageService.getBaseStorageLocation().resolve(directory);
        try {
            if (Files.exists(dirPath)) {
                try (var paths = Files.list(dirPath)) {
                    paths.filter(Files::isRegularFile).forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            throw new ValidationException("Could not delete existing image: " + path.getFileName());
                        }
                    });
                }
            }

            for (MultipartFile file : files) {
                String mimeType = tika.detect(file.getInputStream());
                if (!SUPPORTED_IMAGE_TYPES.contains(mimeType)) {
                    throw new ValidationException("Unsupported file type: " + mimeType);
                }

                String extension = getExtensionFromMime(mimeType);
                String filename = UUID.randomUUID().toString() + "." + extension;
                fileStorageService.saveFile(directory, filename, file);
            }

        } catch (IOException e) {
            throw new ValidationException(i18n("exception.bad.request"));
        }
    }


    public List<String> getProductVariationSecondaryImagePaths(UUID productId, UUID variationId) {
        String directory = "products/" + productId + "/variations/" + variationId + "/secondary_images";
        Path dirPath = fileStorageService.getBaseStorageLocation().resolve(directory);

        if (!Files.exists(dirPath) || !Files.isDirectory(dirPath)) {
            return List.of();
        }

        try (var paths = Files.list(dirPath)) {
            return paths
                    .filter(Files::isRegularFile)
                    .map(path -> {
                        String filename = path.getFileName().toString();
                        int dotIndex = filename.lastIndexOf('.');
                        String filenameWithoutExtension = (dotIndex != -1) ? filename.substring(0, dotIndex) : filename;
                        return baseUrl + "/image/product-variation-secondary-image/"
                                + productId + "/" + variationId + "/" + filenameWithoutExtension;
                    })
                    .toList();
        } catch (IOException e) {
            return List.of();
        }
    }




    private Path getEntityImagePath(EntityType entityType, UUID entityId, String extension,UUID parentId) {
        String directory = getDirectory(entityType, entityId,parentId);
        String filename = entityId + "." + extension;
        return fileStorageService.getFilePath(directory, filename);
    }

    public Path getSecondaryImagePath(UUID productId, UUID variationId, String filenameWithoutExtension) {
        String directory = "products/" + productId + "/variations/" + variationId + "/secondary_images";

        for (String mimeType : SUPPORTED_IMAGE_TYPES) {
            String extension = getExtensionFromMime(mimeType);
            String fullFilename = filenameWithoutExtension + "." + extension;
            Path path = fileStorageService.getFilePath(directory, fullFilename);
            if (Files.exists(path) && Files.isRegularFile(path)) {
                return path;
            }
        }

        throw new ValidationException("Secondary image not found for filename: " + filenameWithoutExtension);
    }



    public Path getExistingImagePath(EntityType entityType, UUID entityId, UUID parentId) {
        for (String mimeType : SUPPORTED_IMAGE_TYPES) {
            String extension = getExtensionFromMime(mimeType);
            Path path = getEntityImagePath(entityType, entityId, extension, parentId);
            if (Files.exists(path)) {
                return path;
            }
        }
        throw new ValidationException("No image found for entity: " + entityId);
    }


    private String getDirectory(EntityType entityType, UUID entityId, UUID parentId) {
        return switch (entityType) {
            case USER -> "users";
            case PRODUCT -> "products/" + entityId;
            case PRODUCT_VARIATION -> {
                if (parentId == null) {
                    throw new ValidationException("Product ID (parentId) is required for product_variation");
                }
                yield "products/" + parentId + "/variations/" + entityId;
            }
        };
    }


    private String getExtensionFromMime(String mimeType) {
        return switch (mimeType) {
            case "image/jpeg" -> "jpg";
            case "image/png" -> "png";
            case "image/bmp" -> "bmp";
            default -> throw new ValidationException("Unsupported MIME type: " + mimeType);
        };
    }


    public String getUserImagePath(UUID userId) {
        try {
            Path imagePath = getExistingImagePath(EntityType.USER, userId, null);
            return baseUrl + "/image/profile-image/" + userId;
        } catch (ValidationException ex) {
            return null;
        }
    }

    public String getProductImagePath(UUID productId) {
        try {
            Path imagePath = getExistingImagePath(EntityType.PRODUCT, productId, null);
            return baseUrl + "/image/product-image/" + productId;
        } catch (ValidationException ex) {
            return null;
        }
    }

    public String getProductVariationImagePath(UUID productId, UUID variationId) {
        try {
            Path imagePath = getExistingImagePath(EntityType.PRODUCT_VARIATION, variationId, productId);
            return baseUrl + "/image/product-variation-image/" + productId + "/" + variationId;
        } catch (ValidationException ex) {
            return null;
        }
    }

}

