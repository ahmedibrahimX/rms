package com.example.rms.controller.mapper;

import com.example.rms.controller.model.OrderRequest;
import com.example.rms.controller.model.PlacedOrderItemResponse;
import com.example.rms.controller.model.PlacedOrderResponse;
import com.example.rms.service.model.OrderPreparationDetails;
import com.example.rms.service.model.PlacedOrderDetails;
import com.example.rms.service.model.RequestedOrderItemDetails;

import java.util.UUID;

public class OrderDetailsMapper {
    public static OrderPreparationDetails map(UUID branchId, UUID customerId, OrderRequest orderRequest) {
        var orderedItems = orderRequest.products().stream().map(i -> new RequestedOrderItemDetails(i.productId(), i.quantity())).toList();
        return new OrderPreparationDetails(branchId, customerId, orderedItems);
    }

    public static PlacedOrderResponse map(PlacedOrderDetails details) {
        var orderItems = details.orderItems().stream().map(i -> new PlacedOrderItemResponse(i.orderItemId(), i.productId())).toList();
        return new PlacedOrderResponse(details.orderId(), details.branchId(), details.customerId(), details.status(), orderItems);
    }
}
