package com.preeti.sansarcart.projection;

import com.fasterxml.jackson.annotation.JsonProperty;

public interface SellerListView extends UserListView {

    @JsonProperty("company_contact")
    String getCompanyContactNumber();
    @JsonProperty("gst_number")
    String getGstNumber();
    @JsonProperty("company_name")
    String getCompanyName();
}
