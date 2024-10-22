package com.example.rms.service.abstraction;

import com.example.rms.service.model.abstraction.NewOrderWithConsumption;
import com.example.rms.service.model.implementation.PlacedOrderDetails;
import com.example.rms.service.pattern.pipeline.Step;

public interface OrderPlacementStep extends Step<NewOrderWithConsumption, PlacedOrderDetails> {
}
