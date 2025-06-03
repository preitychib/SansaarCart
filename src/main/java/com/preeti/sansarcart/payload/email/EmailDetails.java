package com.preeti.sansarcart.payload.email;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class EmailDetails {
    private String to;
    private String subject;
    private String text;
}
