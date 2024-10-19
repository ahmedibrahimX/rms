package com.example.rms.infra.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import org.springframework.data.domain.Persistable;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
@Setter
@Getter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Accessors(fluent = true, chain = true)
@Entity
@Table(name = "ingredient_stock")
public class IngredientStock implements Persistable<UUID> {
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

    @Column(name = "max_capacity_in_kilos")
    private BigDecimal maxCapacityInKilos;

    @Transient
    @Builder.Default
    private boolean isNewEntry = true;

    public IngredientStock(UUID id, UUID branchId, Long ingredientId, BigDecimal amountInKilos, BigDecimal maxCapacityInKilos) {
        this(id, new Branch().id(branchId), branchId, new Ingredient().id(ingredientId), ingredientId, amountInKilos, maxCapacityInKilos, true);
    }

    // copy constructor
    public IngredientStock(IngredientStock that) {
        this(that.id, new Branch().id(that.branchId), that.branchId, new Ingredient().id(that.ingredientId), that.ingredientId, that.amountInKilos, that.maxCapacityInKilos, false);
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        return isNewEntry;
    }
}
