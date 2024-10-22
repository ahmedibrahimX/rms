package com.example.rms.service.model.abstraction;

import java.util.List;
import java.util.UUID;

public interface PersistedOrderDetails<T extends PersistedOrderItemDetails> extends OrderBase<T> {
        Long orderId();
        UUID branchId();
        UUID customerId();
        String status();
        List<T> orderItems();
}
