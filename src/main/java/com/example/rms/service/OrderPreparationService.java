package com.example.rms.service;

import com.example.rms.infra.entity.Order;
import com.example.rms.infra.entity.OrderItem;
import com.example.rms.infra.repo.OrderItemRepo;
import com.example.rms.infra.repo.OrderRepo;
import com.example.rms.service.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OrderPreparationService {
    private final OrderRepo orderRepo;
    private final OrderItemRepo orderItemRepo;
    public OrderPreparationService(OrderRepo orderRepo, OrderItemRepo orderItemRepo) {
        this.orderRepo = orderRepo;
        this.orderItemRepo = orderItemRepo;
    }

    public List<IngredientAmount> getTotalAmountsInGrams(List<ProductRecipe> recipes, List<RequestedProductDetails> requestedProductDetails) {
        Map<Long, Integer> quantities = requestedProductDetails.stream().collect(Collectors.toMap(RequestedProductDetails::productId, RequestedProductDetails::quantity));
        Map<Long, IngredientAmount> amounts = new HashMap<>();
        recipes.forEach(recipe -> {
            recipe.ingredientAmounts().forEach(ingredientAmountInRecipe -> {
                Long ingredientId = ingredientAmountInRecipe.ingredientId();
                IngredientAmount totalAmount = amounts.getOrDefault(ingredientId, new IngredientAmount(ingredientId, 0));
                Integer totalAmountPerProduct = ingredientAmountInRecipe.amountInGrams() * quantities.get(recipe.productId());
                amounts.put(ingredientId, new IngredientAmount(ingredientId, totalAmount.amountInGrams() + totalAmountPerProduct));
            });
        });
        return amounts.values().stream().toList();
    }

    public OrderDetails place(List<RequestedProductDetails> requestedProductDetails, UUID customerId, UUID branchId) {
        Order newOrder = orderRepo.save(new Order(branchId, customerId, "PLACED"));
        List<OrderItem> orderItems = new ArrayList<>();
        for (var requestedDetails : requestedProductDetails) {
            Long productId = requestedDetails.productId();
            Integer count = requestedDetails.quantity();
            for (int i = 0; i < count; i++) {
                orderItems.add(new OrderItem(productId, newOrder.id()));
            }
        }
        orderItemRepo.saveAll(orderItems);

        List<OrderItemDetails> orderItemDetails = orderItems.stream().map(item -> new OrderItemDetails(item.productId())).toList();
        return new OrderDetails(newOrder.id(), newOrder.branchId(), newOrder.customerId(), newOrder.status(), orderItemDetails);
    }
}
