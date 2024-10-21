package com.example.rms.service;

import com.example.rms.service.model.IngredientAmount;
import com.example.rms.service.model.OrderPreparationDetails;
import com.example.rms.service.model.RequestedOrderItemDetails;
import com.example.rms.service.model.interfaces.OrderWithConsumption;
import com.example.rms.service.model.interfaces.OrderWithRecipe;
import com.example.rms.service.pattern.pipeline.Step;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ConsumptionCalculationService implements Step<OrderWithRecipe, OrderWithConsumption> {

    public OrderWithConsumption process(OrderWithRecipe order) {
        Map<Long, Integer> productQuantity = order.orderItems().stream().collect(Collectors.toMap(RequestedOrderItemDetails::productId, RequestedOrderItemDetails::quantity));
        Map<Long, IngredientAmount> ingredientConsumptionAmounts = new HashMap<>();
        order.recipes().forEach(recipe -> {
            recipe.ingredientAmounts().forEach(ingredientAmountInRecipe -> {
                Long ingredientId = ingredientAmountInRecipe.ingredientId();
                IngredientAmount totalAmount = ingredientConsumptionAmounts.getOrDefault(ingredientId, new IngredientAmount(ingredientId, 0));
                Integer totalAmountPerProduct = ingredientAmountInRecipe.amountInGrams() * productQuantity.get(recipe.productId());
                ingredientConsumptionAmounts.put(ingredientId, new IngredientAmount(ingredientId, totalAmount.amountInGrams() + totalAmountPerProduct));
            });
        });
        return new OrderPreparationDetails(order, ingredientConsumptionAmounts.values().stream().toList());
    }
}
