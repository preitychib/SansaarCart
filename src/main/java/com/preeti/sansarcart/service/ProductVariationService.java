package com.preeti.sansarcart.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.preeti.sansarcart.common.Util;
import com.preeti.sansarcart.entity.*;
import com.preeti.sansarcart.exception.custom.ListValidationException;
import com.preeti.sansarcart.exception.custom.ResourceNotFound;
import com.preeti.sansarcart.exception.custom.ValidationException;
import com.preeti.sansarcart.payload.product.ProductVariationDTO;
import com.preeti.sansarcart.payload.product.ProductVariationUpdateDTO;
import com.preeti.sansarcart.repository.ProductRepository;
import com.preeti.sansarcart.repository.ProductVariationRepository;
import com.preeti.sansarcart.response.ProductVariationResponse;
import com.preeti.sansarcart.service.image.ImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static com.preeti.sansarcart.common.I18n.i18n;
import static com.preeti.sansarcart.common.Util.sanitize;
import static com.preeti.sansarcart.common.Util.sanitizeJsonNode;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductVariationService {

    private final ProductRepository productRepository;
    private final ProductVariationRepository productVariationRepository;
    private final ImageService imageService;
    private final AuthenticationService authenticationService;

    @Transactional
    public ProductVariationDTO createProductVariation(UUID productId, ProductVariationDTO dto) {
        log.debug("Creating product variation for productId: {}", productId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFound(i18n("exception.product.not.found")));

        if (!product.getActive() || product.getDeleted()) {
            log.warn("Variation creation attempted for inactive or deleted product: {}", productId);
            throw new ValidationException(i18n("validation.variation.product.inactive"));
        }

        JsonNode metaData = dto.getMetaData();
        validateProductVariationUniqueness(productId, metaData);
        Map<String, Set<String>> validFields = getMetaDataCategoryMap(productId);
        List<String> errors = validateMetaData(metaData, validFields);

        if (!errors.isEmpty()) {
            log.debug("Validation errors for product variation metadata: {}", errors);
            throw new ListValidationException(errors);
        }

        ProductVariation variation = dto.toEntity(product);
        variation.setActive(true);

        ProductVariation saved = productVariationRepository.save(variation);
        product.getVariations().add(variation);
        productRepository.save(product);

        log.info("Successfully created product variation for productId: {}", productId);
        return dto.fromEntity(saved);
    }


    public ProductVariationResponse sellerViewProductVariation(UUID variationId, UUID userId) {
        ProductVariation pv = productVariationRepository.findById(variationId)
                .orElseThrow(()->new ResourceNotFound(i18n("exception.product.variation.not.found")));
        Product product = pv.getProduct();
        if (!product.getSeller().getId().equals(userId)) {
            throw new AccessDeniedException(i18n("exception.access.denied"));
        }

        return toProductVariationResponse(pv);
    }

    public ProductVariationDTO updateProductVariation(UUID variationId, UUID userId, ProductVariationUpdateDTO updateDTO){
        ProductVariation variation = productVariationRepository.findById(variationId)
                .orElseThrow(()->new ResourceNotFound(i18n("exception.product.variation.not.found")));

        Product product = variation.getProduct();
        if (!product.getSeller().getId().equals(userId)) {
            throw new AccessDeniedException(i18n("exception.access.denied"));
        }

        if (!product.getActive()) {
            throw new ValidationException("validation.variation.product.inactive");
        }
        if(updateDTO!=null){
            if (updateDTO.getMetaData() != null ){
                JsonNode metaData = updateDTO.getMetaData();
                validateProductVariationUniqueness(product.getId(), metaData);
                Map<String, Set<String>> validFields = getMetaDataCategoryMap(product.getId());
                List<String> errors = validateMetaData(metaData, validFields);
                if (!errors.isEmpty()) {
                    log.debug("Validation errors for product variation update metadata: {}", errors);
                    throw new ListValidationException(errors);
                }
            }

            variation = updateDTO.patchVariation(variation);
            productVariationRepository.save(variation);
        }
        ProductVariationDTO response = new ProductVariationDTO();
        return response.fromEntity(variation);
    }


    public List<ProductVariationResponse> getVariationsByProductId(UUID productId, int page, int size, String sortBy, String sortDir) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFound(i18n("product.not.found", productId)));

        Seller currentUser = (Seller)authenticationService.getCurrentUser();

//        if (!product.getSeller().getId().equals(currentUser.getId())) {
//            throw new AccessDeniedException(i18n("exception.access.denied"));
//        }

        Comparator<ProductVariation> comparator = switch (sortBy.toLowerCase()) {
            case "price" -> Comparator.comparing(ProductVariation::getPrice);
            case "quantityavailable" -> Comparator.comparing(ProductVariation::getQuantityAvailable);
            default -> Comparator.comparing(ProductVariation::getId);
        };

        if (sortDir.equalsIgnoreCase("desc")) {
            comparator = comparator.reversed();
        }

        List<ProductVariationResponse> all = product.getVariations().stream()
                .filter(ProductVariation::getActive)
                .sorted(comparator)
                .skip((long) page * size)
                .limit(size)
                .map(this::toProductVariationResponse)
                .toList();

        return all;
    }


    public ProductVariationResponse toProductVariationResponse(ProductVariation pv) {
        Product product = pv.getProduct();
        return new ProductVariationResponse(
                pv.getId(),
                pv.getQuantityAvailable(),
                pv.getPrice(),
                pv.getMetaData(),
                product.getId(),
                product.getProductName(),
                product.getDescription(),
                imageService.getProductVariationImagePath(product.getId(), pv.getId()),
                imageService.getProductVariationSecondaryImagePaths(product.getId(), pv.getId())
        );
    }

    /*
   this function get all the metadata of the product
   this only include all the metadata value of the immediate child in case the parent also contain the same field
   this ensure that some redundant values are not in the child in some cases and child can have specialized values
    */
    public Map<String, Set<String>> getMetaDataCategoryMap(UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFound(i18n("exception.product.not.found")));

        Map<String, Set<String>> fieldValueMap = new HashMap<>();
        Category current = product.getCategory();

        while (current != null) {
            for (CategoryMetaDataValue meta : current.getMetaValues()) {
                String fieldName = sanitize(meta.getCategoryMetaDataField().getName());

                // Only use the first occurrence to prioritize child category's specialization
                if (!fieldValueMap.containsKey(fieldName)) {
                    Set<String> values = Arrays.stream(meta.getValue().split(","))
                            .map(Util::sanitize)
                            .collect(Collectors.toSet());
                    fieldValueMap.put(fieldName, values);
                }
            }
            current = current.getParent();
        }

        log.debug("Resolved metadata fields for productId {}: {}", productId, fieldValueMap.keySet());
        return fieldValueMap;
    }

    /**
     * Validates the structure and values of the incoming metadata against allowed fields.
     */
    private List<String> validateMetaData(JsonNode metaData, Map<String, Set<String>> validFields) {
        List<String> errors = new ArrayList<>();

        if (metaData == null || !metaData.fieldNames().hasNext()) {
            errors.add(i18n("validation.variation.metadata.empty"));
            return errors;
        }

        metaData.fieldNames().forEachRemaining(field -> {
            if (!validFields.containsKey(field)) {
                errors.add(i18n("validation.variation.metadata.invalid.field") + ": " + field);
            } else {
                String value = metaData.get(field).asText();
                if (value == null || value.isEmpty()) {
                    errors.add(i18n("validation.variation.metadata.empty.value") + ": " + field);
                } else {
                    Set<String> allowedValues = validFields.get(field);
                    boolean isValid = allowedValues.stream()
                            .anyMatch(allowedValue -> allowedValue.equalsIgnoreCase(value));
                    if (!isValid) {
                        errors.add(i18n("validation.variation.metadata.invalid.value")
                                .replace("{field}", field).replace("{value}", value));
                    }
                }
            }
        });

        return errors;
    }

    /**
     * Ensures metadata structure matches the first saved variation, and checks for duplication.
     */
    private void validateProductVariationUniqueness(UUID productId, JsonNode newMetaData) {
        List<ProductVariation> existingVariations = productVariationRepository.findByProductId(productId);

        if (!existingVariations.isEmpty()) {
            ProductVariation firstVariation = existingVariations.getFirst();
            JsonNode existingMeta = firstVariation.getMetaData();

            Set<String> newKeys = new HashSet<>();
            newMetaData.fieldNames().forEachRemaining(newKeys::add);

            Set<String> existingKeys = new HashSet<>();
            existingMeta.fieldNames().forEachRemaining(existingKeys::add);

            if (!existingKeys.equals(newKeys)) {
                log.warn("Metadata structure mismatch for productId {}. Existing keys: {}, New keys: {}",
                        productId, existingKeys, newKeys);
                throw new ValidationException(i18n("validation.variation.metadata.structure.mismatch"));
            }
        }

        boolean isDuplicate = existingVariations.stream()
                .anyMatch(existing -> sanitizeJsonNode(existing.getMetaData()).equals(sanitizeJsonNode(newMetaData)));

        if (isDuplicate) {
            log.warn("Duplicate metadata detected for productId {}", productId);
            throw new ValidationException(i18n("validation.variation.metadata.structure.duplicate"));
        }
    }
}
