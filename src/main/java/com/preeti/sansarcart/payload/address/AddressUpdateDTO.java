package com.preeti.sansarcart.payload.address;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.preeti.sansarcart.entity.Address;
import com.preeti.sansarcart.enums.AddressLabelType;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.function.Consumer;

@Getter
@Setter
public class AddressUpdateDTO {

    @Pattern(regexp = "^(?=.*[a-zA-Z]|$)[a-zA-Z0-9\\s\\-_,.()]*$", message = "{validation.string.invalid}")
    private String name;

    @Pattern(regexp = "^(?=.*[a-zA-Z]|$)[a-zA-Z0-9\\s\\-_,.()]*$", message = "{validation.string.invalid}")
    private String state;

    @Pattern(regexp = "^(?=.*[a-zA-Z]|$)[a-zA-Z0-9\\s\\-_,.()]*$", message = "{validation.string.invalid}")
    private String city;

    @Pattern(regexp = "^(?=.*[a-zA-Z]|$)[a-zA-Z0-9\\s\\-_,.()]*$", message = "{validation.string.invalid}")
    private String country;

    @JsonProperty("address_line")
    @Pattern(regexp = "^(?=.*[a-zA-Z]|$)[a-zA-Z0-9\\s\\-_,.()]*$", message = "{validation.string.invalid}")
    private String addressLine;

    @Digits(integer = 6, fraction = 0, message = "Invalid zip code")
    @JsonProperty("zip_code")
    private Long zipCode;

    private AddressLabelType label;

    public <T> void applyIfPresent( T value, Consumer<T> setter) {
        if (value!=null) {
            setter.accept(value);
        }
    }
    public Address patchAddress(Address address) {
        applyIfPresent(getCity(), address::setCity);
        applyIfPresent(getState(), address::setState);
        applyIfPresent(getZipCode(), address::setZipCode);
        applyIfPresent(getAddressLine(), address::setAddressLine);
        applyIfPresent(getCountry(), address::setCountry);
        applyIfPresent(getLabel(), address::setLabel);
        return  address;
    }
}
