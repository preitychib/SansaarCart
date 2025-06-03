package com.preeti.sansarcart.projection;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.preeti.sansarcart.enums.AddressLabelType;

import java.util.UUID;

public interface AddressView {

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
