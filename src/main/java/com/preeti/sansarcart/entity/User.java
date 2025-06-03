package com.preeti.sansarcart.entity;

import java.time.Instant;
import java.util.*;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDateTime;


@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_user_email", columnList = "email"),
        @Index(name = "idx_user_is_deleted", columnList = "is_deleted")
})
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
@NoArgsConstructor
@DynamicUpdate
public class User extends AuditInfo{

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(nullable = false, length = 60)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @Column(nullable = false)
    private String firstName;

    private String middleName;

    private String lastName;


    @Column(name = "is_deleted")
    private boolean deleted = false;

    @Column(name = "is_active")
    private boolean active = false;

    @Column(name = "is_expired")
    private boolean passwordExpired = false;

    @Column(name = "is_locked")
    private boolean accountLocked = false;

    private Integer invalidAttemptCount = 0;

    @Transient
    private Instant lockedAt;

    @Column(name = "password_update_date")
    private LocalDateTime passwordUpdateDate;


    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Address> addresses = new ArrayList<>();

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", lastName='" + lastName + '\'' +
                ", firstName='" + firstName + '\'' +
                '}';
    }



    @PrePersist
    public void prePersist() {
        if (this.passwordUpdateDate == null) {
            this.passwordUpdateDate = LocalDateTime.now();
        }
    }

    @PreUpdate
    public void preUpdate() {
        if (this.password != null) {
            this.passwordUpdateDate = LocalDateTime.now();
        }
    }

}

