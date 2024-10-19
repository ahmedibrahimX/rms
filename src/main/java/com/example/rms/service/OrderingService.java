package com.example.rms.service;

import com.example.rms.infra.entity.IngredientStock;
import com.example.rms.infra.entity.Order;
import com.example.rms.infra.entity.OrderItem;
import com.example.rms.infra.repo.*;
import com.example.rms.service.model.RequestedProductDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

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

        Set<Long> ingredientIds = consumedIngredientAmountInGrams.keySet();
        Map<Long, IngredientStock> currentStocks = ingredientStockRepo.findByBranchIdAndIngredientIdIn(branchId, ingredientIds).stream()
                .collect(Collectors.toMap(IngredientStock::ingredientId, stock -> stock));

        List<IngredientStock> updatedStocks = new ArrayList<>();
        for(var currentStock: currentStocks.entrySet()) {
            Long ingredientId = currentStock.getKey();
            IngredientStock updated = new IngredientStock(currentStock.getValue());
            BigDecimal previousAmountInKilos = updated.amountInKilos();
            BigDecimal consumedAmountInKilos = BigDecimal.valueOf(consumedIngredientAmountInGrams.get(ingredientId))
                    .divide(BigDecimal.valueOf(1000), 3, RoundingMode.HALF_UP);
            BigDecimal updatedAmountInKilos = previousAmountInKilos.subtract(consumedAmountInKilos);
            updated.amountInKilos(updatedAmountInKilos);
            updatedStocks.add(updated);
        }
        ingredientStockRepo.saveAll(updatedStocks);

        Order newOrder = orderRepo.save(new Order(branchId, customerId, "PLACED"));
        List<OrderItem> orderItems = new ArrayList<>();
        for (var product: productCounts.entrySet()) {
            Long productId = product.getKey();
            Integer count = product.getValue();
            for (int i=0; i<count; i++) {
                orderItems.add(new OrderItem(productId, newOrder.id()));
            }
        }
        orderItemRepo.saveAll(orderItems);
        return newOrder;
    }
}
