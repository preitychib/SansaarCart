package com.preeti.sansarcart.payload.product;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.preeti.sansarcart.entity.Category;
import com.preeti.sansarcart.entity.Product;
import com.preeti.sansarcart.entity.Seller;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

import java.util.UUID;

import static com.preeti.sansarcart.common.Util.sanitize;

@Getter
@Setter
public class ProductDTO {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private UUID id;

    @NotBlank(message = "{product.name.required}")
    @JsonProperty("product_name")
    @Pattern(regexp = "^(?=.*[a-zA-Z]|$)[a-zA-Z0-9\\s\\-_,.()]*$", message = "{validation.string.invalid}")
    private String productName;

    @JsonProperty("description")
    @Pattern(
            regexp = "^(?=.*[a-zA-Z]|$)[a-zA-Z0-9\\s\\-_,.()]*$",
            message = "{validation.string.invalid}"
    )
    private String description;

    @NotBlank(message = "{product.brand.required}")
    @Pattern(regexp = "^(?=.*[a-zA-Z]|$)[a-zA-Z0-9\\s\\-_,.()]*$", message = "{validation.string.invalid}")
    private String brand;

    @JsonProperty("is_cancellable")
    private Boolean cancellable;

    @JsonProperty("is_returnable")
    private Boolean returnable;

    @JsonProperty(value="is_active", access = JsonProperty.Access.READ_ONLY)
    private Boolean active;

    @NotNull(message = "{product.category.id.required}")
    @JsonProperty("category_id")
    private UUID categoryId;

    @JsonProperty(value="seller_id", access = JsonProperty.Access.READ_ONLY)
    private UUID sellerId;

    public void setProductName(String productName) {
        this.productName = sanitize(productName);
    }

    public void setBrand(String brand) {
        this.brand = sanitize(brand);
    }

    public void setDescription(String description) {
        this.description = sanitize(description);
    }

    public Product toProduct(Seller seller, Category category) {
        Product product = new Product();
        BeanUtils.copyProperties(this, product);
        product.setSeller(seller);
        product.setCategory(category);
        return product;
    }

    public static ProductDTO from(Product product) {
        ProductDTO dto = new ProductDTO();
        BeanUtils.copyProperties(product, dto);
        dto.setSellerId(product.getSeller() != null ? product.getSeller().getId() : null);
        dto.setCategoryId(product.getCategory() != null ? product.getCategory().getId() : null);
        return dto;
    }
}
