package com.preeti.sansarcart.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.preeti.sansarcart.entity.Seller;
import com.preeti.sansarcart.entity.User;
import com.preeti.sansarcart.enums.EntityType;
import com.preeti.sansarcart.exception.custom.ListValidationException;
import com.preeti.sansarcart.exception.custom.ValidationException;
import com.preeti.sansarcart.payload.product.ProductDTO;
import com.preeti.sansarcart.payload.product.ProductUpdateDTO;
import com.preeti.sansarcart.payload.product.ProductVariationDTO;
import com.preeti.sansarcart.payload.product.ProductVariationUpdateDTO;
import com.preeti.sansarcart.response.ApiResponse;
import com.preeti.sansarcart.response.MetaData;
import com.preeti.sansarcart.response.ProductViewResponse;
import com.preeti.sansarcart.response.ProductVariationResponse;
import com.preeti.sansarcart.service.AuthenticationService;
import com.preeti.sansarcart.service.ProductService;
import com.preeti.sansarcart.service.ProductVariationService;
import com.preeti.sansarcart.service.image.ImageService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.util.StringUtils;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

import static com.preeti.sansarcart.common.I18n.i18n;


@RestController
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductService productService;
    private final ProductVariationService variationService;
    private final AuthenticationService authenticationService;
    private final ImageService imageService;
    private final ObjectMapper objectMapper;
    private final Validator validator;

// ================================= SELLER API ====================================

    // Create product seller api
    @PostMapping("/seller/product")
    public ResponseEntity<ApiResponse<ProductDTO>> createProduct(
            @Valid @RequestBody ProductDTO productDTO) {
        User currentUser= authenticationService.getCurrentUser();
        log.info("Starting product creation for user: {}", currentUser.getId());
        ProductDTO created = productService.createProduct(currentUser.getId(), productDTO);
        log.info("Product created successfully with ID: {}", created.getId());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(i18n("product.create.success"),created));
    }

    // Create product variation of given id
    @PostMapping(value = "seller/product/{productId}/variation", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ProductVariationDTO>> createVariation(
            @PathVariable UUID productId,
            @RequestPart("data") @Valid ProductVariationDTO dto,
            @RequestPart("primaryImage") MultipartFile primaryImage,
            @RequestPart(value = "secondaryImages", required = false) List<MultipartFile> secondaryImages
    ) {
        log.info("Starting product variation creation for product ID: {}", productId);

        ProductVariationDTO created = variationService.createProductVariation(productId, dto);
        imageService.saveEntityImage(EntityType.PRODUCT_VARIATION, created.getId(), primaryImage, productId);
        if(secondaryImages!=null)
            imageService.saveSecondaryImages(productId, created.getId(), secondaryImages);
        created.setPrimaryImageName(imageService.getProductVariationImagePath(productId, created.getId()));
        created.setSecondaryImages(imageService.getProductVariationSecondaryImagePaths(productId, created.getId()));

        log.info("Product variation created successfully with ID: {}", created.getId());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(i18n("product.variation.created.success"), created));
    }


    // get seller product by id can be non-active
    @GetMapping("seller/product/{productId}")
    public ResponseEntity<ApiResponse<ProductViewResponse>> getProductWithCategoryDetails(
            @PathVariable UUID productId) {
        Seller currentUser = (Seller)authenticationService.getCurrentUser();
        log.info("Fetching product with category details for product ID: {} and seller ID: {}", productId, currentUser.getId());
        ProductViewResponse productCategoryResponse = productService.sellerGetProductWithCategoryDetail(productId, currentUser.getId());
        log.info("Product details fetched successfully for product ID: {}", productId);

        return ResponseEntity.ok(ApiResponse.success(i18n("product.fetch.success"), productCategoryResponse));
    }

    // get product variation by id can be non-active
    @GetMapping("seller/product-variation/{productVariationId}")
    public ResponseEntity<ApiResponse<ProductVariationResponse>> getProductVariationDetails(
            @PathVariable UUID productVariationId) {
        Seller currentUser = (Seller)authenticationService.getCurrentUser();
        ProductVariationResponse productVariationResponse = variationService.sellerViewProductVariation(productVariationId,currentUser.getId());

        return ResponseEntity.ok(ApiResponse.success(i18n("product.variation.fetch.success"),productVariationResponse));
    }

    // seller get all product created by seller that can also be non-active
    @GetMapping("/seller/products")
    public ResponseEntity<ApiResponse<List<ProductViewResponse>>> sellerGetAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "productName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) String productName
    ) {
        if (!sortDir.equalsIgnoreCase("asc") && !sortDir.equalsIgnoreCase("desc")) {
            throw new ValidationException(i18n("validation.sort.direction.invalid", sortDir));
        }
        log.info("Fetching seller products with pagination - Page: {}, Size: {}, Sort: {} {}, Filters: categoryId: {}, productName: {}",
                page, size, sortBy, sortDir, categoryId,productName);

        Seller currentUser = (Seller)authenticationService.getCurrentUser();
        UUID sellerId= currentUser.getId();
        Map<String, String> filters = new HashMap<>();
        // make sure only seller products are fetched
        filters.put("sellerId",sellerId.toString() );

        if (categoryId != null) filters.put("categoryId", categoryId.toString());
        if (StringUtils.hasText(productName)) filters.put("productName", productName);
        List<ProductViewResponse> productList = productService.getAllProducts(page, size, sortBy, sortDir, categoryId, sellerId , productName);

        log.info("Successfully fetched seller {} products", productList.size());
        MetaData metaData = MetaData.ofPagination(size, page, sortBy, sortDir, filters);
        return ResponseEntity.ok(ApiResponse.success(i18n("product.list.fetch.success"), productList, metaData));
    }

    // seller get all product variation can be non-active
    @GetMapping("/seller/product/{productId}/variations")
    public ResponseEntity<ApiResponse<List<ProductVariationResponse>>> getProductVariations(
            @PathVariable UUID productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "price") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        if (!sortDir.equalsIgnoreCase("asc") && !sortDir.equalsIgnoreCase("desc")) {
            throw new ValidationException(i18n("validation.sort.direction.invalid", sortDir));
        }

        log.info("Fetching product variations for product ID: {} with pagination - Page: {}, Size: {}, Sort: {} {}", productId, page, size, sortBy, sortDir);
        List<ProductVariationResponse> pagedResult = variationService.getVariationsByProductId(
                productId, page, size, sortBy, sortDir
        );

        log.info("Successfully fetched {} product variations for product ID: {}", pagedResult.size(), productId);
        MetaData metaData = MetaData.ofPagination(size, page, sortBy, sortDir, Map.of("productId", productId.toString()));
        return ResponseEntity.ok(ApiResponse.success(i18n("product.variation.list.fetched"), pagedResult, metaData));
    }



    // seller delete product by id
    @DeleteMapping("seller/product/{productId}")
    public ResponseEntity<ApiResponse<String>> deleteProduct(@PathVariable UUID productId) {
        Seller currentUser = (Seller)authenticationService.getCurrentUser();
        log.info("Starting product deletion for product ID: {} and seller ID: {}", productId, currentUser.getId());
        productService.deleteProduct(currentUser, productId);
        log.info("Product with ID: {} deleted successfully by seller ID: {}", productId, currentUser.getId());

        return ResponseEntity.ok(ApiResponse.success(i18n("product.deleted.success"),null));
    }

    // seller update product by id
    @PutMapping("seller/product/{productId}")
    public ResponseEntity<ApiResponse<ProductDTO>> updateProduct(@PathVariable UUID productId, @RequestBody ProductUpdateDTO dto) {
        Seller currentUser = (Seller)authenticationService.getCurrentUser();
        log.info("Starting product update for product ID: {} and seller ID: {}", productId, currentUser.getId());
        ProductDTO updatedProductDTO = productService.updateProduct(currentUser, productId, dto);
        log.info("Product with ID: {} updated successfully by seller ID: {}", productId, currentUser.getId());

        return ResponseEntity.ok(ApiResponse.success(i18n("product.update.success"), updatedProductDTO));
    }


    // update product variation by id
    @PutMapping(value = "seller/product/{productId}/variation/{variationId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ProductVariationDTO>> updateVariation(
            @PathVariable UUID productId,
            @PathVariable UUID variationId,
            @RequestPart(value = "data", required = false) String data,
            @RequestPart(value = "primaryImage", required = false) MultipartFile primaryImage,
            @RequestPart(value = "secondaryImages", required = false) List<MultipartFile> secondaryImages
    ) {
        try {
            ProductVariationUpdateDTO dto = null;
            if (data != null && !data.isBlank()) {
                dto = objectMapper.readValue(data, ProductVariationUpdateDTO.class);
                Set<ConstraintViolation<ProductVariationUpdateDTO>> violations = validator.validate(dto);
                if (!violations.isEmpty()) {
                    List<String> errorMessages = violations.stream()
                            .map(ConstraintViolation::getMessage)
                            .collect(Collectors.toList());

                    throw new ListValidationException(errorMessages);
                }
            }

            Seller currentUser = (Seller) authenticationService.getCurrentUser();
            log.info("Starting update of product variation for product ID: {} and variation ID: {} by seller ID: {}", productId, variationId, currentUser.getId());

            ProductVariationDTO updated = variationService.updateProductVariation(variationId, currentUser.getId(), dto);
            log.info("Product variation update initiated for variation ID: {} by seller ID: {}", variationId, currentUser.getId());

            if (primaryImage != null && !primaryImage.isEmpty()) {
                imageService.saveEntityImage(EntityType.PRODUCT_VARIATION, updated.getId(), primaryImage, productId);
            }

            if (secondaryImages != null && !secondaryImages.isEmpty()) {
                imageService.saveSecondaryImages(productId, updated.getId(), secondaryImages);
            }

            updated.setPrimaryImageName(imageService.getProductVariationImagePath(productId, updated.getId()));
            updated.setSecondaryImages(imageService.getProductVariationSecondaryImagePaths(productId, updated.getId()));

            log.info("Product variation with ID: {} updated successfully with new images by seller ID: {}", updated.getId(), currentUser.getId());
            return ResponseEntity.ok(ApiResponse.success(i18n("product.variation.updated.success"), updated));
        } catch (JsonProcessingException e) {
            throw new ValidationException(e.getMessage());
        }
    }



//================================= ADMIN API ====================================


    // admin view product api can be non-active
    @GetMapping("admin/product/{productId}")
    public ResponseEntity<ApiResponse<ProductViewResponse>> adminGetProductDetails(
            @PathVariable UUID productId) {
        log.info("Admin: Fetching product with  for product ID: {}", productId);
        ProductViewResponse productResponse = productService.adminGetProductWithVariationDetail(productId);
        log.info("Admin: Product details fetched successfully for product ID: {}", productId);

        return ResponseEntity.ok(ApiResponse.success(i18n("product.fetch.success"), productResponse));
    }

    // view all products
    @GetMapping("/admin/products")
    public ResponseEntity<ApiResponse<List<ProductViewResponse>>> adminGetAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "productName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) UUID sellerId,
            @RequestParam(required = false) String productName
    ) {
        if (!sortDir.equalsIgnoreCase("asc") && !sortDir.equalsIgnoreCase("desc")) {
            throw new ValidationException(i18n("validation.sort.direction.invalid", sortDir));
        }
        log.info("Admin: Fetching  products with pagination - Page: {}, Size: {}, Sort: {} {}, Filters: categoryId: {}, sellerId: {}, productName: {}",
                page, size, sortBy, sortDir, categoryId, sellerId, productName);

        Map<String, String> filters = new HashMap<>();
        if (categoryId != null) filters.put("categoryId", categoryId.toString());
        if (sellerId != null) filters.put("sellerId", sellerId.toString());
        if (StringUtils.hasText(productName)) filters.put("productName", productName);
        List<ProductViewResponse> productList = productService.getAllProducts(page, size, sortBy, sortDir, categoryId, sellerId, productName);

        log.info("Admin: Successfully fetched {}  products", productList.size());
        MetaData metaData = MetaData.ofPagination(size, page, sortBy, sortDir, filters);
        return ResponseEntity.ok(ApiResponse.success(i18n("product.list.fetch.success"), productList, metaData));
    }

    // admin activate and deactivate product
    @PutMapping("/admin/product/{productId}/change-activation-status")
    public ResponseEntity<ApiResponse<Void>> changeProductActivationStatus(@PathVariable UUID productId){
        log.info("Admin: Toggling product {} active status",productId.toString());
        String status=productService.toggleProductActivation(productId);
        log.info("Admin: Successfully toggled product: {}  active status", productId.toString());
        return ResponseEntity.ok(ApiResponse.success(status,null));
    }


    // get all the valid metadata of the product this is meant for self testing
    @GetMapping("/admin/product/{productId}/metadata")
    public ResponseEntity<ApiResponse<?>> getProductCategoryMetadata(@PathVariable UUID productId) {
        log.info("Admin: Fetching product meta data for self reference {}",productId.toString());
        Map<String, Set<String>> metadata = productService.getMetaDataCategoryMap(productId);
        log.info("Admin: Successfully fetched product meta data for self reference {}",productId.toString());
        return ResponseEntity.ok(ApiResponse.success(i18n("product.metadata.success"),metadata));
    }


//================================= Customer ====================================

    // customer get product by id with at-least one variation
    @GetMapping("customer/product/{productId}")
    public ResponseEntity<ApiResponse<ProductViewResponse>> customerGetProductDetails(
            @PathVariable UUID productId) {
        log.info("Customer: Fetching product with  for product ID: {}", productId);
        ProductViewResponse productResponse = productService.customerGetProductWithVariationDetail(productId);
        log.info("Customer: Product details fetched successfully for product ID: {}", productId);

        return ResponseEntity.ok(ApiResponse.success(i18n("product.fetch.success"), productResponse));
    }

    // customer get all products with at-least one variation
    @GetMapping("/customer/products")
    public ResponseEntity<ApiResponse<List<ProductViewResponse>>> getAllActiveProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "productName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) UUID sellerId,
            @RequestParam(required = false) String productName
    ) {
        if (!sortDir.equalsIgnoreCase("asc") && !sortDir.equalsIgnoreCase("desc")) {
            throw new ValidationException(i18n("validation.sort.direction.invalid", sortDir));
        }
        log.info("Fetching active products with pagination - Page: {}, Size: {}, Sort: {} {}, Filters: categoryId: {}, sellerId: {}, productName: {}",
                page, size, sortBy, sortDir, categoryId, sellerId, productName);

        Map<String, String> filters = new HashMap<>();
        if (categoryId != null) filters.put("categoryId", categoryId.toString());
        if (sellerId != null) filters.put("sellerId", sellerId.toString());
        if (StringUtils.hasText(productName)) filters.put("productName", productName);
        List<ProductViewResponse> productList = productService.customerGetAllActiveProducts(page, size, sortBy, sortDir, categoryId, sellerId, productName);

        log.info("Successfully fetched {} active products", productList.size());
        MetaData metaData = MetaData.ofPagination(size, page, sortBy, sortDir, filters);
        return ResponseEntity.ok(ApiResponse.success(i18n("product.list.fetch.success"), productList, metaData));
    }


    // get all similar products of the given product
    @GetMapping("/customer/product/{productId}/similar")
    public ResponseEntity<ApiResponse<List<ProductViewResponse>>> getSimilarProducts(
            @PathVariable UUID productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "similarityScore") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String productName
    ) {
        if (!sortDir.equalsIgnoreCase("asc") && !sortDir.equalsIgnoreCase("desc")) {
            throw new ValidationException(i18n("validation.sort.direction.invalid", sortDir));
        }

        log.info("Fetching similar products for product ID: {} with pagination - Page: {}, Size: {}, Sort: {} {}",
                productId, page, size, sortBy, sortDir);
        Map<String, String> filters = new HashMap<>();
        if (StringUtils.hasText(productName)) filters.put("productName", productName);

        List<ProductViewResponse> pagedSimilarProducts = productService.findSimilarProducts(
                productId, page, size, sortBy, sortDir,productName
        );

        log.info("Successfully fetched {} similar products for product ID: {}", pagedSimilarProducts.size(), productId);
        MetaData metaData = MetaData.ofPagination(size, page, sortBy, sortDir, filters);
        return ResponseEntity.ok(ApiResponse.success(i18n("product.similar.fetched"), pagedSimilarProducts, metaData));
    }


}
