package com.example.rms.service.abstraction;

import com.example.rms.service.model.abstraction.NewOrder;
import com.example.rms.service.model.abstraction.NewOrderWithRecipe;
import com.example.rms.service.pattern.pipeline.Step;

public interface RecipeStep extends Step<NewOrder, NewOrderWithRecipe> {
}
