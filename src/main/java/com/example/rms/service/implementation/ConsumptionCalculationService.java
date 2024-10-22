package com.example.rms.service.implementation;

import com.example.rms.service.abstraction.ConsumptionCalculationStep;
import com.example.rms.service.model.abstraction.IngredientAmount;
import com.example.rms.service.model.implementation.NewOrderPreparationDetails;
import com.example.rms.service.model.implementation.RequestedOrderItemDetails;
import com.example.rms.service.model.abstraction.NewOrderWithConsumption;
import com.example.rms.service.model.abstraction.NewOrderWithRecipe;
import com.example.rms.service.model.implementation.ConsumptionIngredientAmount;
import com.example.rms.service.pattern.pipeline.Step;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ConsumptionCalculationService implements ConsumptionCalculationStep {

    public NewOrderWithConsumption process(NewOrderWithRecipe order) {
        Map<Long, Integer> productQuantity = order.orderItems().stream().collect(Collectors.toMap(RequestedOrderItemDetails::productId, RequestedOrderItemDetails::quantity));
        Map<Long, ConsumptionIngredientAmount> ingredientConsumptionAmounts = new HashMap<>();
        order.recipes().forEach(recipe -> {
            recipe.ingredientAmounts().forEach(ingredientAmountInRecipe -> {
                Long ingredientId = ingredientAmountInRecipe.ingredientId();
                IngredientAmount totalAmount = ingredientConsumptionAmounts.getOrDefault(ingredientId, new ConsumptionIngredientAmount(ingredientId, 0));
                Integer totalAmountPerProduct = ingredientAmountInRecipe.amountInGrams() * productQuantity.get(recipe.productId());
                ingredientConsumptionAmounts.put(ingredientId, new ConsumptionIngredientAmount(ingredientId, totalAmount.amountInGrams() + totalAmountPerProduct));
            });
        });
        return new NewOrderPreparationDetails(order, ingredientConsumptionAmounts.values().stream().toList());
    }
}
