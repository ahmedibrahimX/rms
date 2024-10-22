package com.example.rms.infra.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;

import java.util.Date;
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

    @Column(name = "placed_at", updatable = false, nullable = false)
    private Date placedAt;

    public Order(UUID branchId, UUID customerId, String status) {
        this(null, branchId, customerId, status);
    }

    public Order(Long id, UUID branchId, UUID customerId, String status) {
        this(id, branchId, customerId, status, new Date());
    }

    public Order(Long id, UUID branchId, UUID customerId, String status, Date placedAt) {
        this(id, new Branch().id(branchId), branchId, new Customer().id(customerId), customerId, status, placedAt);
    }
}
