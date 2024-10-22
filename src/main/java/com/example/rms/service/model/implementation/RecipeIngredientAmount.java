package com.example.rms.service.model.implementation;

import com.example.rms.service.model.abstraction.IngredientAmount;

public record RecipeIngredientAmount(Long ingredientId, Integer amountInGrams) implements IngredientAmount {
}
