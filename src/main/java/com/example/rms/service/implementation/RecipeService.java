package com.example.rms.service.implementation;

import com.example.rms.infra.entity.ProductIngredient;
import com.example.rms.infra.repo.ProductIngredientRepo;
import com.example.rms.service.abstraction.RecipeStep;
import com.example.rms.service.model.abstraction.NewOrder;
import com.example.rms.service.model.abstraction.NewOrderWithRecipe;
import com.example.rms.service.model.implementation.NewOrderPreparationDetails;
import com.example.rms.service.model.implementation.ProductRecipe;
import com.example.rms.service.model.implementation.RecipeIngredientAmount;
import com.example.rms.service.model.implementation.RequestedOrderItemDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RecipeService implements RecipeStep {
    private final ProductIngredientRepo productIngredientRepo;

    @Autowired
    public RecipeService(ProductIngredientRepo productIngredientRepo) {
        this.productIngredientRepo = productIngredientRepo;
    }

    public NewOrderWithRecipe process(NewOrder order) {
        Set<Long> productIds = order.orderItems().stream().map(RequestedOrderItemDetails::productId).collect(Collectors.toSet());
        List<ProductIngredient> productIngredients = productIngredientRepo.findAllByProductIdIn(productIds);

        Map<Long, ProductRecipe> recipes = new HashMap<>();
        productIngredients.forEach(productIngredient -> {
            Long productId = productIngredient.productId();
            ProductRecipe recipe = recipes.getOrDefault(productId, new ProductRecipe(productId, new ArrayList<>()));
            recipe.ingredientAmounts().add(new RecipeIngredientAmount(productIngredient.ingredientId(), productIngredient.amountInGrams()));
            recipes.put(productId, recipe);
        });
        return new NewOrderPreparationDetails(order, recipes.values().stream().toList());
    }
}
