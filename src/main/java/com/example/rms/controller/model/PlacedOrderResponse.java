package com.example.rms.controller.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.UUID;

public record PlacedOrderResponse(
        @JsonProperty("orderId")
        Long orderId,
        @JsonProperty("branchId")
        UUID branchId,
        @JsonProperty("customerId")
        UUID customerId,
        @JsonProperty("status")
        String status,
        @JsonProperty("orderItems")
        List<PlacedOrderItemResponse> orderItems) {
}
