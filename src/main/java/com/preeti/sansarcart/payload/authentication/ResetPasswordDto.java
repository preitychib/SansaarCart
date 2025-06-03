package com.preeti.sansarcart.payload.authentication;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.preeti.sansarcart.exception.custom.ValidationException;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import static com.preeti.sansarcart.common.I18n.i18n;

@Getter
@Setter
public class ResetPasswordDto {
    @NotBlank(message = "{validation.password.required}")
    @Size(min = 8, max = 15, message = "{validation.password.length}")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).{8,15}$",
            message = "{validation.password.pattern}"
    )
    protected String password;

    @JsonProperty("confirm_password")
    @NotBlank(message = "{validation.confirm.password.required}")
    protected String confirmPassword;

    public void validatePasswords() {
        if (!password.equals(confirmPassword)) {
            throw new ValidationException(i18n("validation.password.mismatch"));
        }
    }
}
