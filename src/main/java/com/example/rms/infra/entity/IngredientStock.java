package com.example.rms.infra.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.UUID;

@Setter
@Getter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Accessors(fluent = true, chain = true)
@Entity
@Table(name = "ingredient_stock")
public class IngredientStock {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", referencedColumnName = "id")
    private Branch branch;
    @Column(name = "branch_id", nullable = false, insertable = false, updatable = false)
    private UUID branchId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id", referencedColumnName = "id")
    private Ingredient ingredient;
    @Column(name = "ingredient_id", nullable = false, insertable = false, updatable = false)
    private Long ingredientId;

    @Column(name = "amount_in_kilos")
    private BigDecimal amountInKilos;

    public IngredientStock(UUID id, UUID branchId, Long ingredientId, BigDecimal amountInKilos) {
        this(id, new Branch().id(branchId), branchId, new Ingredient().id(ingredientId), ingredientId, amountInKilos);
    }
}
