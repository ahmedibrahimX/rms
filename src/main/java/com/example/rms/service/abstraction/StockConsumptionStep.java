package com.example.rms.service.abstraction;

import com.example.rms.service.model.abstraction.NewOrderWithConsumption;
import com.example.rms.service.pattern.pipeline.Step;

public interface StockConsumptionStep extends Step<NewOrderWithConsumption, NewOrderWithConsumption> {
}
