package com.preeti.sansarcart.entity;

import com.preeti.sansarcart.enums.TokenType;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "tokens")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Token {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(unique = true, nullable = false, length = 64)
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TokenType type;

    @OneToOne(optional = false)
    private User user;

    @Column(nullable = false)
    private Instant expiry;

    public boolean isExpired() {
        return expiry.isBefore(Instant.now());
    }

}
