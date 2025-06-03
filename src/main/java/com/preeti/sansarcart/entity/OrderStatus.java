package com.preeti.sansarcart.entity;


import com.preeti.sansarcart.enums.OrderStatusType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "order_status")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatus extends AuditInfo{

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private OrderStatusType fromStatus;

    private OrderStatusType toStatus;

    private String transitionNotesComment;
    private LocalDateTime transitionDate;

    @ManyToOne
    @JoinColumn(name = "order_product_id")
    private OrderProduct orderProduct;
}
