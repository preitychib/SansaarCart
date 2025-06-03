package com.preeti.sansarcart.payload.authentication;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.preeti.sansarcart.entity.Seller;
import com.preeti.sansarcart.payload.address.AddressDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

@Getter
@Setter
public class SellerRegisterDto extends UserRegisterDto {

    @JsonProperty("company_name")
    @NotBlank(message = "{validation.companyName.required}")
    @Pattern(regexp = "^(?=.*[a-zA-Z]|$)[a-zA-Z0-9\\s\\-_,.()]*$", message = "{validation.string.invalid}")
    private String companyName;

    @Pattern(
            regexp = "^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$",
            message = "{validation.gstNumber.invalid}"
    )
    @JsonProperty("gst_number")
    @NotBlank(message = "{validation.gstNumber.required}")
    private String gstNumber;

    @JsonProperty("company_contact")
    @NotBlank(message = "{validation.companyContact.required}")
    @Pattern(
            regexp = "^\\+?[0-9]{10,15}$",
            message = "{validation.companyContact.invalid}"
    )
    private String companyContactNumber;

    @Valid
    @NotNull(message = "{validation.companyAddress.required}")
    @JsonProperty("company_address")
    private AddressDTO address;

    public Seller toSeller() {
        validatePasswords();
        Seller seller = new Seller();
        BeanUtils.copyProperties(this, seller);
        return seller;
    }

    public static SellerRegisterDto from(Seller seller) {
        SellerRegisterDto dto = new SellerRegisterDto();
        BeanUtils.copyProperties(seller, dto);
        return dto;
    }
}
