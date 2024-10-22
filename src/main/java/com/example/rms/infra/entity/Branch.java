package com.example.rms.infra.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;

import java.util.UUID;

@Setter
@Getter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Accessors(fluent = true, chain = true)
@Entity
@Table(name = "branch")
public class Branch {
    @Id
    @Column(name = "id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "merchant_id", referencedColumnName = "id")
    private Merchant merchant;
    @Column(name = "merchant_id", nullable = false, insertable = false, updatable = false)
    private UUID merchantId;

    @Column(name = "building")
    private Integer building;

    @Column(name = "street")
    private String street;

    @Column(name = "region")
    private String region;

    @Column(name = "city")
    private String city;

    @Column(name = "country")
    private String country;

    public Branch(UUID branchId, UUID merchantId, Integer building, String street, String region, String city, String country) {
        this(branchId, new Merchant().id(merchantId), merchantId, building, street, region, city, country);
    }

    @Override
    public String toString() {
        return "Branch{" +
                "id=" + id +
                ", merchantId=" + merchantId +
                ", building=" + building +
                ", street='" + street + '\'' +
                ", region='" + region + '\'' +
                ", city='" + city + '\'' +
                ", country='" + country + '\'' +
                '}';
    }
}
