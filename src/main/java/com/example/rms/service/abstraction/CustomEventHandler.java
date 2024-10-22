package com.example.rms.service.abstraction;

import com.example.rms.service.event.abstraction.CustomEvent;
import com.example.rms.service.model.abstraction.NewOrder;
import com.example.rms.service.pattern.pipeline.Step;

public interface CustomEventHandler<T extends CustomEvent> {
    void handle(T event);
}
