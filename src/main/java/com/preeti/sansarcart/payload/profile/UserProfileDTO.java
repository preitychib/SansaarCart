package com.preeti.sansarcart.payload.profile;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class UserProfileDTO {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private UUID id;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String email;

    @JsonProperty("first_name")
    @Pattern(regexp = "^(?=.*[a-zA-Z]|$)[a-zA-Z0-9\\s\\-_,.()]*$", message = "{validation.string.invalid}")
    private String firstName;

    @JsonProperty("middle_name")
    @Pattern(regexp = "^(?=.*[a-zA-Z]|$)[a-zA-Z0-9\\s\\-_,.()]*$", message = "{validation.string.invalid}")
    private String middleName;

    @JsonProperty("last_name")
    @Pattern(regexp = "^(?=.*[a-zA-Z]|$)[a-zA-Z0-9\\s\\-_,.()]*$", message = "{validation.string.invalid}")
    private String lastName;

    @JsonProperty(value="is_active", access = JsonProperty.Access.READ_ONLY)
    private Boolean active;

    @JsonProperty(value = "profile_image", access = JsonProperty.Access.READ_ONLY)
    private String profileImage;
}

