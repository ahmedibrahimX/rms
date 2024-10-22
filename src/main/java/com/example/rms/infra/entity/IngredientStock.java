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
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Accessors(fluent = true, chain = true)
@Entity
@Table(name = "ingredient_stock")
public class IngredientStock implements Persistable<UUID> {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
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

    @Version
    private Long version;


    public IngredientStock(UUID branchId, Long ingredientId, BigDecimal amountInKilos, BigDecimal maxCapacityInKilos) {
        this(null, new Branch().id(branchId), branchId, new Ingredient().id(ingredientId), ingredientId, amountInKilos, maxCapacityInKilos, true, 0L);
    }
    public IngredientStock(UUID id, UUID branchId, Long ingredientId, BigDecimal amountInKilos, BigDecimal maxCapacityInKilos) {
        this(id, new Branch().id(branchId), branchId, new Ingredient().id(ingredientId), ingredientId, amountInKilos, maxCapacityInKilos, false, 0L);
    }

    // copy constructor
    public IngredientStock(IngredientStock that) {
        this(that.id, new Branch().id(that.branchId), that.branchId, new Ingredient().id(that.ingredientId), that.ingredientId, that.amountInKilos, that.maxCapacityInKilos, false, that.version);
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        return isNewEntry;
    }

    @Override
    public String toString() {
        return "IngredientStock{" +
                "id=" + id +
                ", branchId=" + branchId +
                ", ingredientId=" + ingredientId +
                ", amountInKilos=" + amountInKilos +
                ", maxCapacityInKilos=" + maxCapacityInKilos +
                ", isNewEntry=" + isNewEntry +
                ", version=" + version +
                '}';
    }
}
