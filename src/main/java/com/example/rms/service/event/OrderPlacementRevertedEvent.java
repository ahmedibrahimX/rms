package com.example.rms.service.event;

import com.example.rms.service.model.abstraction.NewOrderWithConsumption;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.springframework.context.ApplicationEvent;

@Getter
@Accessors(fluent = true)
public class OrderPlacementRevertedEvent extends ApplicationEvent {
    NewOrderWithConsumption newOrderWithConsumption;
    public OrderPlacementRevertedEvent(Object source, NewOrderWithConsumption newOrderWithConsumption) {
        super(source);
        this.newOrderWithConsumption = newOrderWithConsumption;
    }
}
