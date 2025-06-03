package com.preeti.sansarcart.payload.profile;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.preeti.sansarcart.entity.Seller;
import com.preeti.sansarcart.payload.address.AddressDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SellerProfileDTO extends UserProfileDTO {

    @JsonProperty("company_name")
    @Pattern(regexp = "^(?=.*[a-zA-Z]|$)[a-zA-Z0-9\\s\\-_,.()]*$", message = "{validation.string.invalid}")
    private String companyName;


    @Pattern(
            regexp = "^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$",
            message = "{validation.gstNumber.invalid}"
    )
    @JsonProperty("gst_number")
    private String gstNumber;

    @JsonProperty("company_contact")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "{validation.companyContact.invalid}")
    private String companyContactNumber;

    private AddressDTO address;


    public static SellerProfileDTO from(Seller seller) {
        SellerProfileDTO dto= new SellerProfileDTO();
        BeanUtils.copyProperties(seller,dto);
        return dto;
    }

    public SellerProfileDTO updateFrom(Seller seller) {
        BeanUtils.copyProperties(seller,this);
        return this;
    }
}
