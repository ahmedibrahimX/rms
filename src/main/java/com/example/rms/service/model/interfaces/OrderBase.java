package com.example.rms.service.model.interfaces;

import com.example.rms.service.model.RequestedOrderItemDetails;

import java.util.List;
import java.util.UUID;

public interface OrderBase {
    UUID branchId();
    UUID customerId();
    List<RequestedOrderItemDetails> orderItems();
}
