package com.example.rms.service.abstraction;

import com.example.rms.service.event.abstraction.OrderPipelineEvent;

public interface OrderPipelineEventHandler<T extends OrderPipelineEvent> {
    void handle(T event);
}
