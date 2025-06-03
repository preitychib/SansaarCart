package com.preeti.sansarcart.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.preeti.sansarcart.entity.Category;
import com.preeti.sansarcart.entity.Product;
import com.preeti.sansarcart.entity.ProductVariation;
import com.preeti.sansarcart.entity.Seller;
import com.preeti.sansarcart.exception.custom.ResourceNotFound;
import com.preeti.sansarcart.exception.custom.ValidationException;
import com.preeti.sansarcart.payload.product.ProductDTO;
import com.preeti.sansarcart.payload.product.ProductUpdateDTO;
import com.preeti.sansarcart.repository.CategoryRepository;
import com.preeti.sansarcart.repository.ProductRepository;
import com.preeti.sansarcart.repository.user.SellerRepository;
import com.preeti.sansarcart.response.ProductViewResponse;
import com.preeti.sansarcart.service.image.ImageService;
import com.preeti.sansarcart.service.similarity.ProductSimilarityStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.preeti.sansarcart.common.I18n.i18n;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final SellerRepository sellerRepository;
    private final EmailService emailService;
    private final ProductVariationService productVariationService;
    private final ImageService imageService;
    private final ProductSimilarityStrategy productSimilarityStrategy;
    private final AuthenticationService authenticationService;

    public ProductDTO createProduct(UUID sellerId, ProductDTO dto) {

        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new ResourceNotFound(i18n("exception.category.not.found")));

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFound(i18n("exception.seller.not.found")));

        List<Category> children = categoryRepository.findAllByParent(category);
        if (!children.isEmpty()) {
            throw new ValidationException(i18n("validation.category.leaf.required"));
        }

        boolean exists = productRepository.findByProductNameAndBrandAndCategoryAndSeller(
                dto.getProductName(), dto.getBrand(), category, seller).isPresent();

        if (exists) {
            throw new ValidationException(i18n("validation.product.brand.category.seller"));
        }

        Product product = dto.toProduct(seller, category);
        product.setActive(false);
        product.setCancellable(dto.getCancellable() != null && dto.getCancellable());
        product.setReturnable( dto.getReturnable()!=null && dto.getReturnable());
        Product saved = productRepository.save(product);
        emailService.sendEmail(EmailBuilderService.buildProductActivationEmailToAdmin(product));
        return ProductDTO.from(saved);

    }

    public ProductDTO updateProduct(Seller seller, UUID productId, ProductUpdateDTO productUpdateDTO){
        Product product = productRepository.findByIdAndSeller(productId, seller)
                .orElseThrow(() -> new ResourceNotFound(i18n("exception.product.not.found")));

        Product updatedProduct = productUpdateDTO.patchProduct(product);
        boolean exists = productRepository.findByProductNameAndBrandAndCategoryAndSeller(
                product.getProductName(), product.getBrand(), product.getCategory(), seller).isPresent();
        if (exists) {
            throw new ValidationException(i18n("validation.product.brand.category.seller"));
        }
        return ProductDTO.from(productRepository.save(updatedProduct));
    }

    public String toggleProductActivation(UUID productId) {
        Product product= productRepository.findById(productId)
                .orElseThrow(()->new ResourceNotFound(i18n("exception.product.not.found")));
        boolean newActivationStatus = !product.getActive();
        product.setActive(newActivationStatus);
        productRepository.save(product);
        if(newActivationStatus){
            emailService.sendEmail(EmailBuilderService.buildProductActivatedEmailToSeller(product));
            return  i18n("product.activation.success");
        }else{
            emailService.sendEmail(EmailBuilderService.buildProductDeactivatedEmailToSeller(product));
            return i18n("product.deactivation.success");
        }
    }

    public ProductDTO getProduct(Seller seller, UUID productId) {
        Product product = productRepository.findByIdAndSeller(productId,seller)
                .orElseThrow(()->new ResourceNotFound(i18n("exception.product.not.found")));
        return ProductDTO.from(product);
    }


    public Map<String, Set<String>> getMetaDataCategoryMap(UUID productId) {
        return productVariationService.getMetaDataCategoryMap(productId);
    }

    public Product getProductById(UUID productId){
        return productRepository.findById(productId)
                .orElseThrow(()->new ResourceNotFound(i18n("exception.product.not.found")));
    }

    public void deleteProduct(Seller seller, UUID productId) {
        Product product = productRepository.findByIdAndSeller(productId, seller)
                .orElseThrow(() -> new ResourceNotFound(i18n("exception.product.not.found")));
        productRepository.delete(product);
    }

    public ProductViewResponse sellerGetProductWithCategoryDetail(UUID productId, UUID userId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFound(i18n("exception.product.not.found")));

        if (!product.getSeller().getId().equals(userId)) {
            throw new AccessDeniedException(i18n("exception.access.denied"));
        }

        return new ProductViewResponse(
                product.getId(),
                product.getBrand(),
                product.getProductName(),
                product.getDescription(),
                product.getCancellable(),
                product.getReturnable(),
                new ProductViewResponse.CategoryDetail(
                        product.getCategory().getId(),
                        product.getCategory().getCategoryName()
                ),
                null,null
        );
    }


    public ProductViewResponse adminGetProductWithVariationDetail(UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFound(i18n("exception.product.not.found")));
        return toProductViewResponse(product,null);

    }

    public ProductViewResponse customerGetProductWithVariationDetail(UUID productId) {
        Product product = productRepository.findByIdAndHasActiveVariations(productId)
                .orElseThrow(() -> new ResourceNotFound(i18n("exception.product.not.found")));
        return toProductViewResponse(product,null);
    }

    public List<ProductViewResponse> customerGetAllActiveProducts(int page, int size, String sortBy, String sortDir,
                                                                  UUID categoryId, UUID sellerId, String productName) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDir), sortBy));
        // this only fetch the active products
        // this only fetch the active products
        Page<Product> products =productRepository.findAllWithFilters(categoryId, sellerId, productName,true, pageable);

        return products.stream().map(product -> toProductViewResponse(product,null)).toList();
    }

    public List<ProductViewResponse> getAllProducts(int page, int size, String sortBy, String sortDir,
                                                                  UUID categoryId, UUID sellerId, String productName) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDir), sortBy));
        Page<Product> products = productRepository.findAllWithFilters(categoryId, sellerId, productName,null, pageable);

        return products.stream().map(product -> toProductViewResponse(product,null)).toList();
    }

    public List<ProductViewResponse> findSimilarProducts(UUID productId, int page, int size, String sortBy, String sortDir, String productName) {
        Product current = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFound(i18n("product.not.found", productId)));

        String currentText = generateSearchText(current);
        List<Product> products = productRepository.findByCategoryAndIdNotFilterByName(current.getCategory(), productId,productName);

        List<ProductViewResponse> similarProducts = new ArrayList<>();

        for (Product product : products) {
            String otherText = generateSearchText(product);
            Double similarity = productSimilarityStrategy.computeSimilarity(currentText, otherText);
            ProductViewResponse dto = toProductViewResponse(product,similarity);
            similarProducts.add(dto);
        }

        Comparator<ProductViewResponse> comparator = Comparator.comparing(dto -> {
            if ("similarityScore".equalsIgnoreCase(sortBy)) return dto.similarityScore();
            return dto.similarityScore();
        });

        if ("desc".equalsIgnoreCase(sortDir)) {
            comparator = comparator.reversed();
        }

        similarProducts.sort(comparator);

        int start = page * size;
        int end = Math.min(start + size, similarProducts.size());
        if (start >= end) return List.of();
        return similarProducts.subList(start, end);
    }


    private String generateSearchText(Product product) {
        StringBuilder sb = new StringBuilder();
        sb.append(product.getProductName()).append(" ");
        for (ProductVariation variation : product.getVariations()) {
            JsonNode metaData = variation.getMetaData();
            metaData.fields().forEachRemaining(entry -> {
                sb.append(entry.getKey()).append(" ");
                sb.append(entry.getValue().asText()).append(" ");
            });
        }
        return sb.toString().toLowerCase();
    }

    private ProductViewResponse toProductViewResponse(Product product, Double similarityScore) {

        return new ProductViewResponse(
                product.getId(),
                product.getProductName(),
                product.getBrand(),
                product.getDescription(),
                product.getCancellable(),
                product.getReturnable(),
                new ProductViewResponse.CategoryDetail(
                        product.getCategory().getId(),
                        product.getCategory().getCategoryName()
                ),
                product.getVariations().stream()
                        .map(variation -> new ProductViewResponse.ProductVariationDetail(
                                variation.getId(),
                                variation.getPrice(),
                                imageService.getProductVariationImagePath(product.getId(),variation.getId())
                        ))
                        .toList()
                , similarityScore
        );
    }



}
