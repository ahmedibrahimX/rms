package com.example.rms.service.abstraction;

import com.example.rms.service.event.implementation.CustomerSyncEvent;

public interface CustomerSyncEventHandler extends CustomerEventHandler<CustomerSyncEvent> {
    void handle(CustomerSyncEvent event);
}
