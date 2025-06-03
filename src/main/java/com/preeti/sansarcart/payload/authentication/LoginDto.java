package com.preeti.sansarcart.payload.authentication;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginDto {

    @NotBlank(message = "{validation.email.required}")
    @Email(message = "{validation.email.invalid}")
    protected String email;

    @NotBlank(message = "{validation.password.required}")
    @Size(min = 8, max = 15, message = "{validation.password.length}")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    protected String password;
}
