package com.example.rms.service.model.abstraction;

import com.example.rms.service.model.implementation.ProductRecipe;
import com.example.rms.service.model.implementation.RequestedOrderItemDetails;

import java.util.List;
import java.util.UUID;

public interface NewOrderWithRecipe extends NewOrder {
    UUID branchId();
    UUID customerId();
    List<RequestedOrderItemDetails> orderItems();
    List<ProductRecipe> recipes();
}
