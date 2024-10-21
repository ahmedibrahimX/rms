package com.example.rms.service.model;

import com.example.rms.service.model.interfaces.OrderBase;
import com.example.rms.service.model.interfaces.OrderWithConsumption;
import com.example.rms.service.model.interfaces.OrderWithRecipe;

import java.util.List;
import java.util.UUID;

public record OrderPreparationDetails(
        UUID branchId,
        UUID customerId,
        List<RequestedOrderItemDetails> orderItems,
        List<ProductRecipe> recipes,
        List<IngredientAmount> consumption
) implements OrderWithConsumption {
    public OrderPreparationDetails(UUID branchId, UUID customerId, List<RequestedOrderItemDetails> orderItems) {
        this(branchId, customerId, orderItems, null, null);
    }

    public OrderPreparationDetails(OrderBase order) {
        this(order.branchId(), order.customerId(), order.orderItems(), null, null);
    }

    public OrderPreparationDetails(OrderBase order, List<ProductRecipe> recipes) {
        this(order.branchId(), order.customerId(), order.orderItems(), recipes, null);
    }

    public OrderPreparationDetails(OrderWithRecipe order, List<IngredientAmount> consumption) {
        this(order.branchId(), order.customerId(), order.orderItems(), order.recipes(), consumption);
    }

    public OrderPreparationDetails(OrderWithConsumption order) {
        this(order.branchId(), order.customerId(), order.orderItems(), order.recipes(), order.consumption());
    }
}
