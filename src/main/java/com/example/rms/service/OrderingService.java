package com.example.rms.service;

import com.example.rms.infra.entity.Order;
import com.example.rms.infra.entity.OrderItem;
import com.example.rms.infra.repo.*;
import com.example.rms.service.model.IngredientAmount;
import com.example.rms.service.model.ProductRecipe;
import com.example.rms.service.model.RequestedProductDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderingService {
    private final OrderRepo orderRepo;
    private final OrderItemRepo orderItemRepo;
    private final RecipeService recipeService;
    private final OrderPreparationService orderPreparationService;
    private final StockService stockService;

    @Autowired
    public OrderingService(OrderRepo orderRepo,
                           OrderItemRepo orderItemRepo,
                           RecipeService recipeService,
                           OrderPreparationService orderPreparationService,
                           StockService stockService) {
        this.orderRepo = orderRepo;
        this.orderItemRepo = orderItemRepo;
        this.recipeService = recipeService;
        this.orderPreparationService = orderPreparationService;
        this.stockService = stockService;
    }

    public Order placeOrder(List<RequestedProductDetails> productRequests, UUID customerId, UUID branchId) {
        Set<Long> productIds = productRequests.stream().map(RequestedProductDetails::productId).collect(Collectors.toSet());
        List<ProductRecipe> recipes = recipeService.getRecipes(productIds);
        List<IngredientAmount> ingredientAmountsInGrams = orderPreparationService.getTotalAmountsInGrams(recipes, productRequests);
        stockService.consumeIngredients(branchId, ingredientAmountsInGrams);

        Order newOrder = orderRepo.save(new Order(branchId, customerId, "PLACED"));
        List<OrderItem> orderItems = new ArrayList<>();
        for (var product : productRequests) {
            Long productId = product.productId();
            Integer count = product.quantity();
            for (int i = 0; i < count; i++) {
                orderItems.add(new OrderItem(productId, newOrder.id()));
            }
        }
        orderItemRepo.saveAll(orderItems);
        return newOrder;
    }
}
