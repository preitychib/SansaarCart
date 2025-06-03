package com.preeti.sansarcart.payload.authentication;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.preeti.sansarcart.entity.Customer;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

@Getter
@Setter
public class CustomerRegisterDto extends UserRegisterDto {

    @JsonProperty("phone_number")
    @NotBlank(message = "{validation.contact.required}")
    @Pattern(
            regexp = "^\\+?[0-9]{10,15}$",
            message = "{validation.contact.invalid}"
    )
    private String phoneNumber;

    public Customer toCustomer() {
        validatePasswords();
        Customer customer = new Customer();
        BeanUtils.copyProperties(this, customer);
        return customer;
    }

    public static CustomerRegisterDto from(Customer customer) {
        CustomerRegisterDto dto = new CustomerRegisterDto();
        BeanUtils.copyProperties(customer, dto);
        return dto;
    }
}

