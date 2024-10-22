package com.example.rms.controller.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.UUID;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

public record PlacedOrderResponse(
        @Schema(example = "1", requiredMode = REQUIRED)
        @JsonProperty("orderId")
        Long orderId,
        @Schema(example = "1", requiredMode = REQUIRED)
        @JsonProperty("branchId")
        UUID branchId,
        @Schema(example = "9a08a617-8116-40aa-adad-0ac072d85282", requiredMode = REQUIRED)
        @JsonProperty("customerId")
        UUID customerId,
        @Schema(example = "PLACED", requiredMode = REQUIRED)
        @JsonProperty("status")
        String status,
        @Schema(requiredMode = REQUIRED)
        @JsonProperty("orderItems")
        List<PlacedOrderItemResponse> orderItems) {
}
