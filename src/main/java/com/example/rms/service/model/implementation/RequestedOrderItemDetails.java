package com.example.rms.service.model.implementation;

import com.example.rms.service.model.abstraction.NewOrderItemDetails;

public record RequestedOrderItemDetails(Long productId, Integer quantity) implements NewOrderItemDetails {
}
