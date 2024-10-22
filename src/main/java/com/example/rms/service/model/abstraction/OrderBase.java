package com.example.rms.service.model.abstraction;

import java.util.List;
import java.util.UUID;

public interface OrderBase<T extends OrderItemDetailsBase> {
    UUID branchId();
    UUID customerId();
    List<T> orderItems();
}
