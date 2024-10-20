package com.example.rms.service.model;

import java.util.List;
import java.util.UUID;

public record OrderDetails(
        Long orderId,
        UUID branchId,
        UUID customerId,
        String status,
        List<OrderItemDetails> orderItems) {
}
