package com.example.rms.service.implementation;

import com.example.rms.service.abstraction.*;
import com.example.rms.service.exception.StockUpdateFailedException;
import com.example.rms.service.model.abstraction.NewOrder;
import com.example.rms.service.model.implementation.PlacedOrderDetails;
import com.example.rms.service.pattern.pipeline.Pipeline;
import com.example.rms.service.pattern.decorator.RetriableStepDecorator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class OrderingPipelineService implements OrderingPipeline {
    private final Pipeline<NewOrder, PlacedOrderDetails> pipeline;

    @Autowired
    public OrderingPipelineService(OrderValidationStep orderValidationStep,
                                   RecipeStep recipeStep,
                                   ConsumptionCalculationStep consumptionCalculationStep,
                                   StockConsumptionStep stockConsumptionStep,
                                   OrderPlacementStep orderPlacementStep,
                                   @Value("${store.update.retrial.max-attempts:3}") Integer stockUpdateMaxAttempts,
                                   @Value("${store.update.retrial.delay.millis:1000}") Long stockUpdateRetrialDelayInMillis,
                                   @Value("${store.update.retrial.delay.multiplier:2}") Integer stockUpdateRetrialDelayMultiplier
                           ) {
        pipeline = new Pipeline<>(orderValidationStep)
                .pipe(recipeStep)
                .pipe(consumptionCalculationStep)
                .pipe(new RetriableStepDecorator<>(stockConsumptionStep, stockUpdateMaxAttempts, stockUpdateRetrialDelayInMillis, stockUpdateRetrialDelayMultiplier, StockUpdateFailedException.class))
                .pipe(orderPlacementStep);
    }

    public PlacedOrderDetails placeOrder(NewOrder order) {
        return pipeline.execute(order);
    }
}
