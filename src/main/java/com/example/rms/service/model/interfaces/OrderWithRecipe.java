package com.example.rms.service.model.interfaces;

import com.example.rms.service.model.ProductRecipe;
import com.example.rms.service.model.RequestedOrderItemDetails;

import java.util.List;
import java.util.UUID;

public interface OrderWithRecipe extends OrderBase {
    UUID branchId();
    UUID customerId();
    List<RequestedOrderItemDetails> orderItems();
    List<ProductRecipe> recipes();
}
