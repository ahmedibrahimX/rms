package com.example.rms.service;

import com.example.rms.infra.entity.Order;
import com.example.rms.infra.entity.OrderItem;
import com.example.rms.infra.repo.OrderItemRepo;
import com.example.rms.infra.repo.OrderRepo;
import com.example.rms.service.model.*;
import com.example.rms.service.model.interfaces.OrderBase;
import com.example.rms.service.pattern.pipeline.Step;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class OrderPlacementService implements Step<OrderBase, PlacedOrderDetails> {
    private final OrderRepo orderRepo;
    private final OrderItemRepo orderItemRepo;
    public OrderPlacementService(OrderRepo orderRepo, OrderItemRepo orderItemRepo) {
        this.orderRepo = orderRepo;
        this.orderItemRepo = orderItemRepo;
    }

    public PlacedOrderDetails process(OrderBase order) {
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
    }
}
