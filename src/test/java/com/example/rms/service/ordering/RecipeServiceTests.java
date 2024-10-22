package com.example.rms.service.ordering;

import com.example.rms.infra.entity.ProductIngredient;
import com.example.rms.infra.repo.ProductIngredientRepo;
import com.example.rms.service.RecipeService;
import com.example.rms.service.model.abstraction.*;
import com.example.rms.service.model.implementation.NewOrderPreparationDetails;
import com.example.rms.service.model.implementation.RequestedOrderItemDetails;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RecipeServiceTests {
    @Mock
    private ProductIngredientRepo productIngredientRepo;
    @InjectMocks
    private RecipeService recipeService;

    private final Long ingredientId1 = 1L;
    private final Long ingredientId2 = 2L;
    private final Long ingredientId3 = 3L;
    private final Long productId1 = 1L;
    private final UUID product1Ingredient1Id = UUID.randomUUID();
    private final ProductIngredient product1Ingredient1 = new ProductIngredient(product1Ingredient1Id, productId1, ingredientId1, 100);
    private final UUID product1Ingredient2Id = UUID.randomUUID();
    private final ProductIngredient product1Ingredient2 = new ProductIngredient(product1Ingredient2Id, productId1, ingredientId2, 50);
    private final Long productId2 = 2L;
    private final UUID product2Ingredient1Id = UUID.randomUUID();
    private final ProductIngredient product2Ingredient1 = new ProductIngredient(product2Ingredient1Id, productId2, ingredientId1, 200);
    private final UUID product2Ingredient3Id = UUID.randomUUID();
    private final ProductIngredient product2Ingredient3 = new ProductIngredient(product2Ingredient3Id, productId2, ingredientId3, 100);
    private final Long productId3 = 3L;
    private final UUID product3Ingredient3Id = UUID.randomUUID();
    private final ProductIngredient product3Ingredient3 = new ProductIngredient(product3Ingredient3Id, productId3, ingredientId3, 50);

    @Test
    @DisplayName("Happy scenario. Get ingredient amounts in grams corresponding to each product. Getting amounts succeeds.")
    public void happyScenario_getIngredientAmountsInGrams_shouldSucceed() throws Exception {
        when(productIngredientRepo.findAllByProductIdIn(any())).thenReturn(List.of(product1Ingredient1, product1Ingredient2, product2Ingredient1, product2Ingredient3, product3Ingredient3));

        List<RequestedOrderItemDetails> requestedItems = List.of(new RequestedOrderItemDetails(productId1, 2),
                new RequestedOrderItemDetails(productId2, 1), new RequestedOrderItemDetails(productId3, 1));
        NewOrder order = new NewOrderPreparationDetails(UUID.randomUUID(), UUID.randomUUID(), requestedItems);
        NewOrderWithRecipe newOrderWithRecipe = recipeService.process(order);

        assertEquals(3, newOrderWithRecipe.recipes().size());
        Map<Long, Map<Long, Integer>> productIngredientAmounts = newOrderWithRecipe.recipes().stream().collect(Collectors.toMap(Recipe::productId, amounts -> amounts.ingredientAmounts().stream().collect(Collectors.toMap(IngredientAmount::ingredientId, IngredientAmount::amountInGrams))));
        assertEquals(product1Ingredient1.amountInGrams(), productIngredientAmounts.get(productId1).get(ingredientId1));
        assertEquals(product1Ingredient2.amountInGrams(), productIngredientAmounts.get(productId1).get(ingredientId2));
        assertEquals(product2Ingredient1.amountInGrams(), productIngredientAmounts.get(productId2).get(ingredientId1));
        assertEquals(product2Ingredient3.amountInGrams(), productIngredientAmounts.get(productId2).get(ingredientId3));
        assertEquals(product3Ingredient3.amountInGrams(), productIngredientAmounts.get(productId3).get(ingredientId3));
    }
}
