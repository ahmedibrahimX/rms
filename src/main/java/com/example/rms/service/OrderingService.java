package com.example.rms.service;

import com.example.rms.infra.entity.Order;
import com.example.rms.infra.entity.OrderItem;
import com.example.rms.infra.repo.*;
import com.example.rms.service.model.RequestedProductDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderingService {
    private final OrderRepo orderRepo;
    private final ProductIngredientRepo productIngredientRepo;
    private final OrderItemRepo orderItemRepo;
    private final StockService stockService;

    @Autowired
    public OrderingService(OrderRepo orderRepo,
                           ProductIngredientRepo productIngredientRepo,
                           OrderItemRepo orderItemRepo,
                           StockService stockService) {
        this.orderRepo = orderRepo;
        this.productIngredientRepo = productIngredientRepo;
        this.orderItemRepo = orderItemRepo;
        this.stockService = stockService;
    }

    public Order placeOrder(List<RequestedProductDetails> productRequests, UUID customerId, UUID branchId) {
        Map<Long, Integer> productCounts = productRequests.stream()
                .collect(Collectors.groupingBy(RequestedProductDetails::productId, Collectors.summingInt(RequestedProductDetails::quantity)));

        Set<Long> productIds = productCounts.keySet();
        Map<Long, Integer> consumedIngredientAmountInGrams = new HashMap<>();
        productIngredientRepo.findAllByProductIdIn(productIds).forEach(productIngredient -> {
            Long ingredientId = productIngredient.ingredientId();
            Long productId = productIngredient.productId();
            Integer amountInGrams = consumedIngredientAmountInGrams.getOrDefault(ingredientId, 0);
            amountInGrams += productIngredient.amountInGrams() * productCounts.get(productId);
            consumedIngredientAmountInGrams.put(ingredientId, amountInGrams);
        });

        stockService.consumeIngredients(branchId, consumedIngredientAmountInGrams);

        Order newOrder = orderRepo.save(new Order(branchId, customerId, "PLACED"));
        List<OrderItem> orderItems = new ArrayList<>();
        for (var product : productCounts.entrySet()) {
            Long productId = product.getKey();
            Integer count = product.getValue();
            for (int i = 0; i < count; i++) {
                orderItems.add(new OrderItem(productId, newOrder.id()));
            }
        }
        orderItemRepo.saveAll(orderItems);
        return newOrder;
    }
}
