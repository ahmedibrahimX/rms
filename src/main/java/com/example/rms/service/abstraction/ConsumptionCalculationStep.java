package com.example.rms.service.abstraction;

import com.example.rms.service.model.abstraction.NewOrderWithConsumption;
import com.example.rms.service.model.abstraction.NewOrderWithRecipe;
import com.example.rms.service.pattern.pipeline.Step;

public interface ConsumptionCalculationStep extends Step<NewOrderWithRecipe, NewOrderWithConsumption> {
}
