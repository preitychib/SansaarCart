package com.preeti.sansarcart.payload.product;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.preeti.sansarcart.entity.Product;
import com.preeti.sansarcart.entity.ProductVariation;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static com.preeti.sansarcart.common.Util.sanitizeJsonNode;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductVariationDTO {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private UUID id;

    @NotNull(message = "{validation.variation.quantity.required}")
    @Min(value = 0, message = "{validation.variation.quantity.min}")
    @JsonProperty("quantity_available")
    private Long quantityAvailable;

    @NotNull(message = "{validation.variation.price.required}")
    @DecimalMin(value = "0.0", inclusive = true, message = "{validation.variation.price.min}")
    private BigDecimal price;

    @NotNull(message = "{validation.variation.metadata.required}")
    @JsonProperty("meta_data")
    private JsonNode metaData;

    @JsonProperty(value = "is_active", access = JsonProperty.Access.READ_ONLY)
    private Boolean active;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private ProductDTO product;

    @JsonProperty(value = "primary_image",access = JsonProperty.Access.READ_ONLY)
    private String primaryImageName;

    @JsonProperty(value = "secondary_images",access = JsonProperty.Access.READ_ONLY)
    private List<String> secondaryImages;


    public JsonNode getMetaData() {
        return sanitizeJsonNode(this.metaData);
    }

    public ProductVariation toEntity(Product product) {
        ProductVariation variation = new ProductVariation();
        BeanUtils.copyProperties(this, variation);
        variation.setProduct(product);
        return variation;
    }

    public ProductVariationDTO fromEntity(ProductVariation variation) {
        BeanUtils.copyProperties(variation, this);
        setProduct(ProductDTO.from(variation.getProduct()));
        return this;
    }

}
