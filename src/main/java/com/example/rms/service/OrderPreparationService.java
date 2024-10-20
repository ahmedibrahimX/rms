package com.example.rms.service;

import com.example.rms.service.model.IngredientAmount;
import com.example.rms.service.model.ProductRecipe;
import com.example.rms.service.model.RequestedProductDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OrderPreparationService {
    public OrderPreparationService() {
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
}