package com.preeti.sansarcart.payload.authentication;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.preeti.sansarcart.exception.custom.ValidationException;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import static com.preeti.sansarcart.common.I18n.i18n;

@Getter
@Setter
public abstract class UserRegisterDto {

    @NotBlank(message = "{validation.email.required}")
    @Email(message = "{validation.email.invalid}")
    protected String email;

    @NotBlank(message = "{validation.password.required}")
    @Size(min = 8, max = 15, message = "{validation.password.length}")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).{8,15}$",
            message = "{validation.password.pattern}"
    )
    protected String password;

    @JsonProperty(value="confirm_password",access = JsonProperty.Access.WRITE_ONLY)
    @NotBlank(message = "{validation.confirm.password.required}")
    protected String confirmPassword;

    @JsonProperty("first_name")
    @NotBlank(message = "{validation.first.name.required}")
    @Pattern(regexp = "^(?=.*[a-zA-Z]|$)[a-zA-Z0-9\\s\\-_,.()]*$", message = "{validation.string.invalid}")
    protected String firstName;

    @JsonProperty("last_name")
    @NotBlank(message = "{validation.last.name.required}")
    @Pattern(regexp = "^(?=.*[a-zA-Z]|$)[a-zA-Z0-9\\s\\-_,.()]*$", message = "{validation.string.invalid}")
    protected String lastName;

    public void validatePasswords() {
        if (!password.equals(confirmPassword)) {
            throw new ValidationException(i18n("validation.password.mismatch"));
        }
    }
}
