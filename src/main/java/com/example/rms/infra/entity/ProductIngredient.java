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
@Table(name = "product_ingredient")
public class ProductIngredient {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", referencedColumnName = "id")
    private Product product;
    @Column(name = "product_id", nullable = false, insertable = false, updatable = false)
    private Long productId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id", referencedColumnName = "id")
    private Ingredient ingredient;
    @Column(name = "ingredient_id", nullable = false, insertable = false, updatable = false)
    private Long ingredientId;

    @Column(name = "amount_in_grams")
    private Integer amountInGrams;

    public ProductIngredient(Long productId, Long ingredientId, Integer amountInGrams) {
        this(null, new Product().id(productId), productId, new Ingredient().id(ingredientId), ingredientId, amountInGrams);
    }

    public ProductIngredient(UUID id, Long productId, Long ingredientId, Integer amountInGrams) {
        this(id, new Product().id(productId), productId, new Ingredient().id(ingredientId), ingredientId, amountInGrams);
    }

    @Override
    public String toString() {
        return "ProductIngredient{" +
                "id=" + id +
                ", productId=" + productId +
                ", ingredientId=" + ingredientId +
                ", amountInGrams=" + amountInGrams +
                '}';
    }
}
