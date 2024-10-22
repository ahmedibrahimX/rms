package com.example.rms.controller.model;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

public record PlacedOrderItemResponse(
        @Schema(example = "25237ee1-2799-4078-aae8-1b5ccfb698ee", requiredMode = REQUIRED)
        UUID orderItemId,
        @Schema(example = "1", requiredMode = REQUIRED)
        Long productId) {
}
