package com.example.rms.service;

import com.example.rms.infra.entity.ProductIngredient;
import com.example.rms.infra.repo.ProductIngredientRepo;
import com.example.rms.service.model.IngredientAmount;
import com.example.rms.service.model.ProductRecipe;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class RecipeService {
    private final ProductIngredientRepo productIngredientRepo;

    @Autowired
    public RecipeService(ProductIngredientRepo productIngredientRepo) {
        this.productIngredientRepo = productIngredientRepo;
    }

    public List<ProductRecipe> getRecipes(Set<Long> productIds) {
        List<ProductIngredient> productIngredients = productIngredientRepo.findAllByProductIdIn(productIds);

        Map<Long, ProductRecipe> recipes = new HashMap<>();
        productIngredients.forEach(productIngredient -> {
            Long productId = productIngredient.productId();
            ProductRecipe recipe = recipes.getOrDefault(productId, new ProductRecipe(productId, new ArrayList<>()));
            recipe.ingredientAmounts().add(new IngredientAmount(productIngredient.ingredientId(), productIngredient.amountInGrams()));
            recipes.put(productId, recipe);
        });
        return recipes.values().stream().toList();
    }
}
