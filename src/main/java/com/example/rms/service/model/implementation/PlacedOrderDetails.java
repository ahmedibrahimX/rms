package com.example.rms.service.model.implementation;

import com.example.rms.service.model.abstraction.PersistedOrderDetails;

import java.util.List;
import java.util.UUID;

public record PlacedOrderDetails(
        Long orderId,
        UUID branchId,
        UUID customerId,
        String status,
        List<PlacedOrderItemDetails> orderItems
) implements PersistedOrderDetails<PlacedOrderItemDetails> {
}
