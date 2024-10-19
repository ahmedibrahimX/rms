package com.example.rms.service;

import com.example.rms.infra.entity.Order;
import com.example.rms.infra.repo.*;
import com.example.rms.service.model.RequestedProductDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class OrderingService {
    private final OrderRepo orderRepo;
    private final ProductRepo productRepo;
    private final ProductIngredientRepo productIngredientRepo;
    private final IngredientStockRepo ingredientStockRepo;
    private final OrderItemRepo orderItemRepo;

    @Autowired
    public OrderingService(OrderRepo orderRepo,
                           ProductRepo productRepo,
                           ProductIngredientRepo productIngredientRepo,
                           IngredientStockRepo ingredientStockRepo,
                           OrderItemRepo orderItemRepo) {
        this.orderRepo = orderRepo;
        this.productRepo = productRepo;
        this.productIngredientRepo = productIngredientRepo;
        this.ingredientStockRepo = ingredientStockRepo;
        this.orderItemRepo = orderItemRepo;
    }

    public Order placeOrder(List<RequestedProductDetails> productRequests, UUID customerId, UUID branchId) {
        return null;
    }
}
