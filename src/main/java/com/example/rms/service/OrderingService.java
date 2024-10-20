package com.example.rms.service;

import com.example.rms.exception.model.InvalidOrderException;
import com.example.rms.infra.entity.Order;
import com.example.rms.infra.entity.OrderItem;
import com.example.rms.infra.repo.*;
import com.example.rms.service.model.IngredientAmount;
import com.example.rms.service.model.OrderDetails;
import com.example.rms.service.model.ProductRecipe;
import com.example.rms.service.model.RequestedProductDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderingService {
    private final OrderValidationService orderValidationService;
    private final RecipeService recipeService;
    private final OrderPreparationService orderPreparationService;
    private final StockService stockService;

    @Autowired
    public OrderingService(OrderValidationService orderValidationService,
                           RecipeService recipeService,
                           OrderPreparationService orderPreparationService,
                           StockService stockService) {
        this.orderValidationService = orderValidationService;
        this.recipeService = recipeService;
        this.orderPreparationService = orderPreparationService;
        this.stockService = stockService;
    }

    public OrderDetails placeOrder(List<RequestedProductDetails> productRequests, UUID customerId, UUID branchId) {
        boolean isValid = orderValidationService.validate(productRequests, branchId);
        if (!isValid) {
            throw new InvalidOrderException();
        }

        Set<Long> productIds = productRequests.stream().map(RequestedProductDetails::productId).collect(Collectors.toSet());
        List<ProductRecipe> recipes = recipeService.getRecipes(productIds);
        List<IngredientAmount> ingredientAmountsInGrams = orderPreparationService.getTotalAmountsInGrams(recipes, productRequests);
        stockService.consumeIngredients(branchId, ingredientAmountsInGrams);
        return orderPreparationService.place(productRequests, customerId, branchId);
    }
}
