package com.preeti.sansarcart.entity;


import com.preeti.sansarcart.enums.AddressLabelType;
import com.preeti.sansarcart.enums.PaymentMethodType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Order extends AuditInfo{

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;


    @ManyToOne
    @JoinColumn(name = "customer_user_id")
    private Customer customer;

    private Double amountPaid;

    private LocalDateTime dateCreated;

    @Enumerated(EnumType.STRING)
    private PaymentMethodType paymentMethod;


    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL,orphanRemoval = true)
    private List<OrderProduct> orderProduct= new ArrayList<>();

    @Column(name = "customer_state")
    private String customerState;

    @Column(name = "customer_country")
    private String customerCountry;

    @Column(name = "customer_address_line")
    private String customerAddressLine;

    @Column(name = "customer_zip_code")
    private Long customerZipCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "customer_address_label")
    private AddressLabelType customerAddressLabel;
}
