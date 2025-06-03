package com.preeti.sansarcart.payload.product;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.preeti.sansarcart.entity.Product;
import com.preeti.sansarcart.entity.Seller;
import com.preeti.sansarcart.entity.Category;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;
import java.util.function.Consumer;

@Getter
@Setter
public class ProductUpdateDTO {

    @JsonProperty("product_name")
    @Pattern(regexp = "^(?=.*[a-zA-Z]|$)[a-zA-Z0-9\\s\\-_,.()]*$", message = "{validation.string.invalid}")
    private String productName;

    @Pattern(regexp = "^(?=.*[a-zA-Z]|$)[a-zA-Z0-9\\s\\-_,.()]*$", message = "{validation.string.invalid}")
    private String description;

    @JsonProperty("is_cancellable")
    private Boolean cancellable;

    @JsonProperty("is_returnable")
    private Boolean returnable;

    public <T> void applyIfPresent(T value, Consumer<T> setter) {
        if (value != null) {
            setter.accept(value);
        }
    }

    public Product patchProduct(Product product) {
        applyIfPresent(getProductName(), product::setProductName);
        applyIfPresent(getDescription(), product::setDescription);
        applyIfPresent(getCancellable(), product::setCancellable);
        applyIfPresent(getReturnable(), product::setReturnable);
        return product;
    }
}
