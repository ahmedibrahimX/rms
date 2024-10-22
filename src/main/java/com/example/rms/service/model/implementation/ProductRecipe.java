package com.example.rms.service.model.implementation;

import com.example.rms.service.model.abstraction.Recipe;

import java.util.List;

public record ProductRecipe(Long productId, List<RecipeIngredientAmount> ingredientAmounts) implements Recipe<RecipeIngredientAmount> {
}
