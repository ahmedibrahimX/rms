package com.example.rms.service.model.abstraction;

import com.example.rms.service.model.implementation.ConsumptionIngredientAmount;

import java.util.List;

public interface NewOrderWithConsumption extends NewOrderWithRecipe {
    List<ConsumptionIngredientAmount> consumption();
}
