package com.preeti.sansarcart.entity;

import com.preeti.sansarcart.enums.RoleType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "authority")
@ToString(of = "authority")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(nullable = false, unique = true)
    @Enumerated(EnumType.STRING)
    private RoleType authority;


    public Role(RoleType roleType){
        this.authority=roleType;
    }


}
