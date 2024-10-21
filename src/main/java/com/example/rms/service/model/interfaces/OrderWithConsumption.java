package com.example.rms.service.model.interfaces;

import com.example.rms.service.model.IngredientAmount;

import java.util.List;

public interface OrderWithConsumption extends OrderWithRecipe {
    List<IngredientAmount> consumption();
}
