package com.example.rms.service.model.implementation;

import com.example.rms.service.model.abstraction.PersistedOrderItemDetails;

import java.util.UUID;

public record PlacedOrderItemDetails(UUID orderItemId, Long productId) implements PersistedOrderItemDetails {
}
