package com.example.rms.service;

import com.example.rms.service.model.abstraction.NewOrder;
import com.example.rms.service.model.implementation.PlacedOrderDetails;
import com.example.rms.service.pattern.pipeline.Pipeline;
import com.example.rms.service.pattern.decorator.RetriableStepDecorator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class OrderingService {
    private final Pipeline<NewOrder, PlacedOrderDetails> pipeline;

    @Autowired
    public OrderingService(OrderValidationService orderValidationService,
                           RecipeService recipeService,
                           ConsumptionCalculationService consumptionCalculationService,
                           StockConsumptionService stockConsumptionService,
                           OrderPlacementService orderPlacementService,
                           @Value("${store.update.retrial.max-attempts:3}") Integer stockUpdateMaxAttempts,
                           @Value("${store.update.retrial.delay.millis:1000}") Long stockUpdateRetrialDelayInMillis,
                           @Value("${store.update.retrial.delay.multiplier:2}") Integer stockUpdateRetrialDelayMultiplier
                           ) {
        pipeline = new Pipeline<>(orderValidationService)
                .pipe(recipeService)
                .pipe(consumptionCalculationService)
                .pipe(new RetriableStepDecorator<>(stockConsumptionService, stockUpdateMaxAttempts, stockUpdateRetrialDelayInMillis, stockUpdateRetrialDelayMultiplier))
                .pipe(orderPlacementService);
    }

    public PlacedOrderDetails placeOrder(NewOrder order) {
        return pipeline.execute(order);
    }
}
