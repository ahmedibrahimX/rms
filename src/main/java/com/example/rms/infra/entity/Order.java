package com.example.rms.infra.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;

import java.util.UUID;

@Setter
@Getter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Accessors(fluent = true, chain = true)
@Entity
@Table(name = "rms_order")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", referencedColumnName = "id")
    private Branch branch;
    @Column(name = "branch_id", nullable = false, insertable = false, updatable = false)
    private UUID branchId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", referencedColumnName = "id")
    private Customer customer;
    @Column(name = "customer_id", nullable = false, insertable = false, updatable = false)
    private UUID customerId;

    @Column(name = "status", nullable = false)
    private String status;

    public Order(Long productId, UUID branchId, UUID customerId, String status) {
        this(productId, new Branch().id(branchId), branchId, new Customer().id(customerId), customerId, status);
    }
}
