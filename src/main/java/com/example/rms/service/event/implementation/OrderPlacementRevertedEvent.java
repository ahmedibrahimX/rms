package com.example.rms.service.event.implementation;

import com.example.rms.service.event.abstraction.OrderPipelineEvent;
import com.example.rms.service.model.abstraction.NewOrderWithConsumption;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.springframework.context.ApplicationEvent;

@Getter
@Accessors(fluent = true)
public class OrderPlacementRevertedEvent extends ApplicationEvent implements OrderPipelineEvent<NewOrderWithConsumption> {
    NewOrderWithConsumption order;
    public OrderPlacementRevertedEvent(Object source, NewOrderWithConsumption order) {
        super(source);
        this.order = order;
    }
}
