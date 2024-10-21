package com.example.rms.service;

import com.example.rms.service.model.*;
import com.example.rms.service.model.interfaces.OrderBase;
import com.example.rms.service.pattern.pipeline.Pipeline;
import com.example.rms.service.pattern.decorator.RetriableStepDecorator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderingService {
    private final Pipeline<OrderBase, PlacedOrderDetails> pipeline;

    @Autowired
    public OrderingService(OrderValidationService orderValidationService,
                           RecipeService recipeService,
                           ConsumptionCalculationService consumptionCalculationService,
                           StockConsumptionService stockConsumptionService,
                           OrderPlacementService orderPlacementService
                           ) {
        pipeline = new Pipeline<>(orderValidationService)
                .pipe(recipeService)
                .pipe(consumptionCalculationService)
                .pipe(new RetriableStepDecorator<>(stockConsumptionService, 3, 1000L, 2))
                .pipe(orderPlacementService);
    }

    public PlacedOrderDetails placeOrder(OrderBase order) {
        return pipeline.execute(order);
    }
}
