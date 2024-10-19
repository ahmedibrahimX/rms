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
@Table(name = "product")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "merchant_id", referencedColumnName = "id")
    private Merchant merchant;
    @Column(name = "merchant_id", nullable = false, insertable = false, updatable = false)
    private UUID merchantId;

    @Column(name = "name")
    private String name;

    public Product(Long productId, UUID merchantId, String name) {
        this(productId, new Merchant().id(merchantId), merchantId, name);
    }
}
