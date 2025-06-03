package com.preeti.sansarcart.payload.address;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.preeti.sansarcart.entity.Address;
import com.preeti.sansarcart.entity.Customer;
import com.preeti.sansarcart.entity.Seller;
import com.preeti.sansarcart.entity.User;
import com.preeti.sansarcart.enums.AddressLabelType;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

import java.util.UUID;

@Getter
@Setter
public class AddressDTO {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private UUID id;

    @NotBlank(message = "{address.city.required}")
    @Pattern(regexp = "^(?=.*[a-zA-Z]|$)[a-zA-Z0-9\\s\\-_,.()]*$", message = "{validation.string.invalid}")
    private String city;

    @NotBlank(message = "{address.state.required}")
    @Pattern(regexp = "^(?=.*[a-zA-Z]|$)[a-zA-Z0-9\\s\\-_,.()]*$", message = "{validation.string.invalid}")
    private String state;

    @NotBlank(message = "{address.country.required}")
    @Pattern(regexp = "^(?=.*[a-zA-Z]|$)[a-zA-Z0-9\\s\\-_,.()]*$", message = "{validation.string.invalid}")
    private String country;

    @NotBlank(message = "{address.line.required}")
    @JsonProperty("address_line")
    @Pattern(regexp = "^(?=.*[a-zA-Z]|$)[a-zA-Z0-9\\s\\-_,.()]*$", message = "{validation.string.invalid}")
    private String addressLine;

    @NotNull(message = "{address.zip.required}")
    @Digits(integer = 6, fraction = 0, message = "{address.zip.invalid}")
    @JsonProperty("zip_code")
    private Long zipCode;


    private AddressLabelType label;

    public Address toCustomerAddress(Customer customer) {
        Address address = new Address();
        BeanUtils.copyProperties(this, address);
        address.setUser(customer);
        return address;
    }

    public Address toUserAddress(User user) {
        Address address = new Address();
        BeanUtils.copyProperties(this, address);
        address.setUser(user);
        return address;
    }

    public Address toSellerAddress(Seller seller) {
        Address address= new Address();
        BeanUtils.copyProperties(this, address);
        address.setLabel(AddressLabelType.SELLER_COMPANY);
        address.setUser(seller);
        return address;
    }

    public static AddressDTO from(Address address) {
        AddressDTO dto = new AddressDTO();
        BeanUtils.copyProperties(address, dto);
        return dto;
    }
}
