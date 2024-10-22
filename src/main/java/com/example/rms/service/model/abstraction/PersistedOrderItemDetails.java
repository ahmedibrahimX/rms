package com.example.rms.service.model.abstraction;

import java.util.UUID;

public interface PersistedOrderItemDetails extends OrderItemDetailsBase {
    UUID orderItemId();
}
