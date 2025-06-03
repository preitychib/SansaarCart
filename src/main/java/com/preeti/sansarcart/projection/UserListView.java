package com.preeti.sansarcart.projection;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.preeti.sansarcart.enums.AddressLabelType;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface UserListView {
    UUID getId();
    @JsonIgnore
    String getFirstName();
    @JsonIgnore
    String getMiddleName();
    @JsonIgnore
    String getLastName();
    String getEmail();
    @JsonProperty("is_active")
    boolean isActive();

    @JsonProperty("full_name")
    default String getFullName() {
        return Stream.of(getFirstName(), getMiddleName(), getLastName())
                .filter(Objects::nonNull)
                .collect(Collectors.joining(" "));
    }

    List<AddressInfo> getAddresses();

    interface AddressInfo {
        UUID getId();
        String getCity();
        String getState();
        String getCountry();
        @JsonProperty("address_line")
        String getAddressLine();
        @JsonProperty("zip_code")
        Long getZipCode();
        AddressLabelType getLabel();
    }
}

