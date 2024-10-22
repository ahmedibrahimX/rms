package com.example.rms.service.model.abstraction;

import com.example.rms.service.model.implementation.RequestedOrderItemDetails;

import java.util.List;
import java.util.UUID;

public interface NewOrder extends OrderBase<RequestedOrderItemDetails> {
    UUID branchId();
    UUID customerId();
    List<RequestedOrderItemDetails> orderItems();
}
