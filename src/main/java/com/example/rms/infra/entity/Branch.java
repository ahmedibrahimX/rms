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
@Table(name = "branch")
public class Branch {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "merchant_id", referencedColumnName = "id")
    private Merchant merchant;
    @Column(name = "merchant_id", nullable = false, insertable = false, updatable = false)
    private UUID merchantId;

    @Column(name = "building")
    private String building;

    @Column(name = "street")
    private String street;

    @Column(name = "region")
    private String region;

    @Column(name = "city")
    private String city;

    @Column(name = "country")
    private String country;

    public Branch(UUID branchId, UUID merchantId, String building, String street, String region, String city, String country) {
        this(branchId, new Merchant().id(merchantId), merchantId, building, street, region, city, country);
    }
}
