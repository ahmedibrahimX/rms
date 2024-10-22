package com.example.rms.controller.mapper;

import com.example.rms.controller.model.OrderRequest;
import com.example.rms.controller.model.PlacedOrderItemResponse;
import com.example.rms.controller.model.PlacedOrderResponse;
import com.example.rms.service.model.implementation.NewOrderPreparationDetails;
import com.example.rms.service.model.implementation.RequestedOrderItemDetails;
import com.example.rms.service.model.implementation.PlacedPersistedOrderDetails;

import java.util.UUID;

public class OrderDetailsMapper {
    public static NewOrderPreparationDetails map(UUID branchId, UUID customerId, OrderRequest orderRequest) {
        var orderedItems = orderRequest.products().stream().map(i -> new RequestedOrderItemDetails(i.productId(), i.quantity())).toList();
        return new NewOrderPreparationDetails(branchId, customerId, orderedItems);
    }

    public static PlacedOrderResponse map(PlacedPersistedOrderDetails details) {
        var orderItems = details.orderItems().stream().map(i -> new PlacedOrderItemResponse(i.orderItemId(), i.productId())).toList();
        return new PlacedOrderResponse(details.orderId(), details.branchId(), details.customerId(), details.status(), orderItems);
    }
}
