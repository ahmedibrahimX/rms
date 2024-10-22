package com.example.rms.service.abstraction;

import com.example.rms.service.model.abstraction.NewOrder;
import com.example.rms.service.model.implementation.PlacedOrderDetails;

public interface OrderingPipeline {
    PlacedOrderDetails placeOrder(NewOrder order);
}
