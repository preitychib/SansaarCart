package com.preeti.sansarcart.projection;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public interface CustomerView {
    UUID getId();
    @JsonProperty("first_name")
    String getFirstName();
    @JsonProperty("middle_name")
    String getMiddleName();
    @JsonProperty("last_name")
    String getLastName();
    String getEmail();
    @JsonProperty("phone_number")
    String getPhoneNumber();

    @JsonProperty("is_active")
    boolean isActive();

    default String getImage(){
        return "test.img";
    }
}
