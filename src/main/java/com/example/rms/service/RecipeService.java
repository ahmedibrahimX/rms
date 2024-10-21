package com.example.rms.service;

import com.example.rms.infra.entity.ProductIngredient;
import com.example.rms.infra.repo.ProductIngredientRepo;
import com.example.rms.service.model.*;
import com.example.rms.service.model.interfaces.OrderBase;
import com.example.rms.service.model.interfaces.OrderWithRecipe;
import com.example.rms.service.pattern.pipeline.Step;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RecipeService implements Step<OrderBase, OrderWithRecipe> {
    private final ProductIngredientRepo productIngredientRepo;

    @Autowired
    public RecipeService(ProductIngredientRepo productIngredientRepo) {
        this.productIngredientRepo = productIngredientRepo;
    }

    public OrderWithRecipe process(OrderBase order) {
        Set<Long> productIds = order.orderItems().stream().map(RequestedOrderItemDetails::productId).collect(Collectors.toSet());
        List<ProductIngredient> productIngredients = productIngredientRepo.findAllByProductIdIn(productIds);

        Map<Long, ProductRecipe> recipes = new HashMap<>();
        productIngredients.forEach(productIngredient -> {
            Long productId = productIngredient.productId();
            ProductRecipe recipe = recipes.getOrDefault(productId, new ProductRecipe(productId, new ArrayList<>()));
            recipe.ingredientAmounts().add(new IngredientAmount(productIngredient.ingredientId(), productIngredient.amountInGrams()));
            recipes.put(productId, recipe);
        });
        return new OrderPreparationDetails(order, recipes.values().stream().toList());
    }
}
