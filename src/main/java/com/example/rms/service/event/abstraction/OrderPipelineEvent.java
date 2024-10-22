package com.example.rms.service.event.abstraction;

import com.example.rms.service.model.abstraction.OrderBase;

public interface OrderPipelineEvent<T extends OrderBase> extends CustomEvent {
    OrderBase order();
}
