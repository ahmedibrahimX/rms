package com.example.rms.service.model;

import java.util.List;

public record ProductRecipe(Long productId, List<IngredientAmount> ingredientAmounts) {
}
