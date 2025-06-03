package com.preeti.sansarcart.payload.product;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.preeti.sansarcart.entity.ProductVariation;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Consumer;

import static com.preeti.sansarcart.common.Util.applyIfPresent;
import static com.preeti.sansarcart.common.Util.sanitizeJsonNode;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductVariationUpdateDTO {

    @Min(value = 0, message = "{validation.variation.quantity.min}")
    @JsonProperty("quantity_available")
    private Long quantityAvailable;

    @DecimalMin(value = "0.0", inclusive = true, message = "{validation.variation.price.min}")
    private BigDecimal price;

    @JsonProperty("meta_data")
    private JsonNode metaData;

    @JsonProperty("is_active")
    private Boolean active;

    public JsonNode getMetaData() {
        return sanitizeJsonNode(this.metaData);
    }


    public ProductVariation patchVariation(ProductVariation variation) {
        applyIfPresent(getQuantityAvailable(), variation::setQuantityAvailable);
        applyIfPresent(getPrice(), variation::setPrice);
        applyIfPresent(getMetaData(), variation::setMetaData);
        applyIfPresent(getActive(), variation::setActive);
        return variation;
    }
}
