package com.example.rms.controller.model;

import java.util.UUID;

public record PlacedOrderItemResponse(UUID orderItemId, Long productId) {
}
