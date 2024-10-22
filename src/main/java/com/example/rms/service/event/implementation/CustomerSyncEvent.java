package com.example.rms.service.event.implementation;

import com.example.rms.service.event.abstraction.CustomerEvent;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

@Getter
@Accessors(fluent = true)
public class CustomerSyncEvent extends ApplicationEvent implements CustomerEvent {
    UUID customerId;
    String customerName;

    public CustomerSyncEvent(Object source, UUID customerId, String customerName) {
        super(source);
        this.customerId = customerId;
        this.customerName = customerName;
    }
}
