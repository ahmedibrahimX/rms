package com.example.rms.service.event;

import com.example.rms.service.model.interfaces.OrderWithConsumption;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.springframework.context.ApplicationEvent;

@Getter
@Accessors(fluent = true)
public class OrderPlacementRevertedEvent extends ApplicationEvent {
    OrderWithConsumption orderWithConsumption;
    public OrderPlacementRevertedEvent(Object source, OrderWithConsumption orderWithConsumption) {
        super(source);
        this.orderWithConsumption = orderWithConsumption;
    }
}
