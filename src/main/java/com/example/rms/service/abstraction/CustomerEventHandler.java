package com.example.rms.service.abstraction;

import com.example.rms.service.event.abstraction.CustomerEvent;

public interface CustomerEventHandler<T extends CustomerEvent> extends CustomEventHandler<T> {
    void handle(T event);
}
