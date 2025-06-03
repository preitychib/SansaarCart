package com.preeti.sansarcart.controller;

import com.preeti.sansarcart.entity.Product;
import com.preeti.sansarcart.entity.User;
import com.preeti.sansarcart.enums.EntityType;
import com.preeti.sansarcart.exception.custom.ResourceNotFound;
import com.preeti.sansarcart.service.AuthenticationService;
import com.preeti.sansarcart.service.ProductService;
import com.preeti.sansarcart.service.image.ImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.preeti.sansarcart.response.ApiResponse;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static com.preeti.sansarcart.common.I18n.i18n;

@RestController
@RequiredArgsConstructor
@RequestMapping("image")
@Slf4j
public class ImageController {

    private final ImageService imageService;
    private final ProductService productService;
    private final AuthenticationService authenticationService;

    @GetMapping("profile-image/{userId}")
    public ResponseEntity<Resource> getUserImage(@PathVariable UUID userId) throws IOException {
        log.info("Fetching profile image for user with ID: {}", userId);
        User currentUser = authenticationService.getCurrentUser();
        if (!currentUser.getId().equals(userId) && !authenticationService.userHasAdminRole(currentUser)) {
            log.error("Access denied: User {} is not authorized to view profile image of user {}", currentUser.getId(), userId);
            throw new ResourceNotFound(i18n("image.fetch.access.denied"));
        }
        Path imagePath = imageService.getExistingImagePath(EntityType.USER, userId, null);
        String mimeType = Files.probeContentType(imagePath);
        Resource resource = new UrlResource(imagePath.toUri());
        log.info("Profile image fetched successfully for user with ID: {}", userId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(mimeType))
                .body(resource);
    }

    @GetMapping("product-image/{productId}")
    public ResponseEntity<Resource> getProductImage(@PathVariable UUID productId) throws IOException {
        log.info("Fetching product image for product with ID: {}", productId);
        Path imagePath = imageService.getExistingImagePath(EntityType.PRODUCT, productId, null);
        String mimeType = Files.probeContentType(imagePath);
        Resource resource = new UrlResource(imagePath.toUri());
        log.info("Product image fetched successfully for product with ID: {}", productId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(mimeType))
                .body(resource);
    }

    @GetMapping("product-variation-image/{productId}/{variationId}")
    public ResponseEntity<Resource> getProductVariationImage(
            @PathVariable UUID productId,
            @PathVariable UUID variationId) throws IOException {
        log.info("Fetching product variation image for product ID: {} and variation ID: {}", productId, variationId);
        Path imagePath = imageService.getExistingImagePath(EntityType.PRODUCT_VARIATION, variationId, productId);
        String mimeType = Files.probeContentType(imagePath);
        Resource resource = new UrlResource(imagePath.toUri());
        log.info("Product variation image fetched successfully for product ID: {} and variation ID: {}", productId, variationId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(mimeType))
                .body(resource);
    }

    @GetMapping("product-variation-secondary-image/{productId}/{variationId}/{secondaryImageId}")
    public ResponseEntity<Resource> getProductVariationSecondaryImage(
            @PathVariable UUID productId,
            @PathVariable UUID variationId, @PathVariable UUID secondaryImageId) throws IOException {
        log.info("Fetching secondary image for product ID: {}, variation ID: {}, secondary image ID: {}", productId, variationId, secondaryImageId);
        Path imagePath = imageService.getSecondaryImagePath(productId, variationId, secondaryImageId.toString());
        String mimeType = Files.probeContentType(imagePath);
        Resource resource = new UrlResource(imagePath.toUri());
        log.info("Secondary image fetched successfully for product ID: {}, variation ID: {}, secondary image ID: {}", productId, variationId, secondaryImageId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(mimeType))
                .body(resource);
    }

    @PostMapping("upload/user/{userId}")
    public ResponseEntity<ApiResponse<Void>> uploadUserProfileImage(
            @PathVariable UUID userId,
            @RequestParam("image") MultipartFile image) throws IOException {
        log.info("Uploading profile image for user with ID: {}", userId);
        User currentUser = authenticationService.getCurrentUser();
        if (!currentUser.getId().equals(userId)) {
            log.error("Access denied: User {} is not authorized to upload profile image for user {}", currentUser.getId(), userId);
            throw new AccessDeniedException(i18n("image.upload.access.denied"));
        }
        imageService.saveEntityImage(EntityType.USER, userId, image, null);
        log.info("Profile image uploaded successfully for user with ID: {}", userId);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(i18n("image.upload.user.success"), null));
    }

    @PostMapping("upload/product/{productId}")
    public ResponseEntity<ApiResponse<Void>> uploadProductImage(
            @PathVariable UUID productId,
            @RequestParam("image") MultipartFile image) throws IOException {
        log.info("Uploading product image for product with ID: {}", productId);
        User currentUser = authenticationService.getCurrentUser();
        Product product = productService.getProductById(productId);
        if (!currentUser.getId().equals(product.getSeller().getId())) {
            log.error("Access denied: User {} is not authorized to upload product image for product {}", currentUser.getId(), productId);
            throw new AccessDeniedException(i18n("image.upload.access.denied"));
        }
        imageService.saveEntityImage(EntityType.PRODUCT, productId, image, null);
        log.info("Product image uploaded successfully for product with ID: {}", productId);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(i18n("image.upload.product.success"), null));
    }

    @PostMapping("upload/product/{productId}/variation/{variationId}")
    public ResponseEntity<ApiResponse<Void>> uploadProductVariationImage(
            @PathVariable UUID productId,
            @PathVariable UUID variationId,
            @RequestParam("image") MultipartFile image) throws IOException {

        log.info("Uploading product variation image for product ID: {} and variation ID: {}", productId, variationId);
        User currentUser = authenticationService.getCurrentUser();
        Product product = productService.getProductById(productId);
        if (!currentUser.getId().equals(product.getSeller().getId())) {
            log.error("Access denied: User {} is not authorized to upload product variation image for product ID: {} and variation ID: {}", currentUser.getId(), productId, variationId);
            throw new AccessDeniedException(i18n("image.upload.access.denied"));
        }
        imageService.saveEntityImage(EntityType.PRODUCT_VARIATION, variationId, image, productId);
        log.info("Product variation image uploaded successfully for product ID: {} and variation ID: {}", productId, variationId);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(i18n("image.upload.variation.success"), null));
    }

    @PostMapping("upload/product/{productId}/variation/{variationId}/secondary-images")
    public ResponseEntity<ApiResponse<Void>> uploadProductVariationSecondaryImages(
            @PathVariable UUID productId,
            @PathVariable UUID variationId,
            @RequestParam("images") List<MultipartFile> images) throws IOException {

        log.info("Uploading secondary images for product ID: {} and variation ID: {}", productId, variationId);
        User currentUser = authenticationService.getCurrentUser();
        Product product = productService.getProductById(productId);
        if (!currentUser.getId().equals(product.getSeller().getId())) {
            log.error("Access denied: User {} is not authorized to upload secondary images for product ID: {} and variation ID: {}", currentUser.getId(), productId, variationId);
            throw new AccessDeniedException(i18n("image.upload.access.denied"));
        }
        imageService.saveSecondaryImages(productId, variationId, images);
        log.info("Secondary images uploaded successfully for product ID: {} and variation ID: {}", productId, variationId);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(i18n("image.upload.variation.secondary.success"), null));
    }


}
