package com.example.rms.service.model.abstraction;

import java.util.List;

public interface Recipe<T extends IngredientAmount> {
    Long productId();
    List<T> ingredientAmounts();
}
