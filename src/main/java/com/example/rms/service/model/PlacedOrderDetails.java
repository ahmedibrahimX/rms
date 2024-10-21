package com.example.rms.service.model;

import java.util.List;
import java.util.UUID;

public record PlacedOrderDetails(
        Long orderId,
        UUID branchId,
        UUID customerId,
        String status,
        List<PlacedOrderItemDetails> orderItems) {
}
