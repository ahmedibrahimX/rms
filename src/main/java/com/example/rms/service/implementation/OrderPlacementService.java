package com.example.rms.service.implementation;

import com.example.rms.infra.entity.Order;
import com.example.rms.infra.entity.OrderItem;
import com.example.rms.infra.repo.OrderItemRepo;
import com.example.rms.infra.repo.OrderRepo;
import com.example.rms.service.abstraction.OrderPlacementStep;
import com.example.rms.service.event.implementation.OrderPlacementRevertedEvent;
import com.example.rms.service.exception.OrderPlacementFailedException;
import com.example.rms.service.model.abstraction.NewOrderWithConsumption;
import com.example.rms.service.model.implementation.PlacedOrderDetails;
import com.example.rms.service.model.implementation.PlacedOrderItemDetails;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class OrderPlacementService implements OrderPlacementStep {
    private final OrderRepo orderRepo;
    private final OrderItemRepo orderItemRepo;
    private final ApplicationEventPublisher eventPublisher;
    public OrderPlacementService(OrderRepo orderRepo, OrderItemRepo orderItemRepo, ApplicationEventPublisher eventPublisher) {
        this.orderRepo = orderRepo;
        this.orderItemRepo = orderItemRepo;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public PlacedOrderDetails process(NewOrderWithConsumption order) {
        try {
            Order newOrder = orderRepo.save(new Order(order.branchId(), order.customerId(), "PLACED"));
            List<OrderItem> orderItems = new ArrayList<>();
            for (var requestedDetails : order.orderItems()) {
                Long productId = requestedDetails.productId();
                Integer count = requestedDetails.quantity();
                for (int i = 0; i < count; i++) {
                    orderItems.add(new OrderItem(productId, newOrder.id()));
                }
            }
            List<OrderItem> placedOrderItems = orderItemRepo.saveAll(orderItems);

            return new PlacedOrderDetails(
                    newOrder.id(),
                    newOrder.branchId(),
                    newOrder.customerId(),
                    newOrder.status(),
                    placedOrderItems.stream().map(item -> new PlacedOrderItemDetails(item.id(), item.productId())).toList()
            );
        } catch (Exception e) {
            eventPublisher.publishEvent(new OrderPlacementRevertedEvent(this, order));
            throw new OrderPlacementFailedException(e);
        }
    }
}