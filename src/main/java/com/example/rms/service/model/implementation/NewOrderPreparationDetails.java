package com.example.rms.service.model.implementation;

import com.example.rms.service.model.abstraction.OrderBase;
import com.example.rms.service.model.abstraction.NewOrderWithConsumption;
import com.example.rms.service.model.abstraction.NewOrderWithRecipe;

import java.util.List;
import java.util.UUID;

public record NewOrderPreparationDetails(
        UUID branchId,
        UUID customerId,
        List<RequestedOrderItemDetails> orderItems,
        List<ProductRecipe> recipes,
        List<ConsumptionIngredientAmount> consumption
) implements NewOrderWithConsumption {
    public NewOrderPreparationDetails(UUID branchId, UUID customerId, List<RequestedOrderItemDetails> orderItems) {
        this(branchId, customerId, orderItems, null, null);
    }

    public NewOrderPreparationDetails(OrderBase order) {
        this(order.branchId(), order.customerId(), order.orderItems(), null, null);
    }

    public NewOrderPreparationDetails(OrderBase order, List<ProductRecipe> recipes) {
        this(order.branchId(), order.customerId(), order.orderItems(), recipes, null);
    }

    public NewOrderPreparationDetails(NewOrderWithRecipe order, List<ConsumptionIngredientAmount> consumption) {
        this(order.branchId(), order.customerId(), order.orderItems(), order.recipes(), consumption);
    }

    public NewOrderPreparationDetails(NewOrderWithConsumption order) {
        this(order.branchId(), order.customerId(), order.orderItems(), order.recipes(), order.consumption());
    }
}
