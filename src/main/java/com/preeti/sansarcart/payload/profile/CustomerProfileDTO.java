package com.preeti.sansarcart.payload.profile;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.preeti.sansarcart.entity.Customer;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CustomerProfileDTO extends UserProfileDTO {

    @JsonProperty("phone_number")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "{validation.customer.invalid}")
    private String phoneNumber;

    public static CustomerProfileDTO from(Customer customer) {
        CustomerProfileDTO dto = new CustomerProfileDTO();
        BeanUtils.copyProperties(customer, dto);
        return dto;
    }
    public CustomerProfileDTO updateFrom(Customer customer) {
        BeanUtils.copyProperties(customer, this);
        return this;
    }

}
