package com.example.rms.service.ordering;

import com.example.rms.infra.entity.IngredientStock;
import com.example.rms.infra.entity.ProductIngredient;
import com.example.rms.service.ConsumptionCalculationService;
import com.example.rms.service.OrderPlacementService;
import com.example.rms.service.model.IngredientAmount;
import com.example.rms.service.model.OrderPreparationDetails;
import com.example.rms.service.model.ProductRecipe;
import com.example.rms.service.model.RequestedOrderItemDetails;
import com.example.rms.service.model.interfaces.OrderBase;
import com.example.rms.service.model.interfaces.OrderWithConsumption;
import com.example.rms.service.model.interfaces.OrderWithRecipe;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
public class ConsumptionCalculationServiceTests {
    @InjectMocks
    private ConsumptionCalculationService consumptionCalculationService;

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
    private final ProductRecipe productRecipe1 = new ProductRecipe(productId1, List.of(new IngredientAmount(ingredientId1, product1Ingredient1.amountInGrams()), new IngredientAmount(ingredientId2, product1Ingredient2.amountInGrams())));
    private final ProductRecipe productRecipe2 = new ProductRecipe(productId2, List.of(new IngredientAmount(ingredientId1, product2Ingredient1.amountInGrams()), new IngredientAmount(ingredientId3, product2Ingredient3.amountInGrams())));
    private final ProductRecipe productRecipe3 = new ProductRecipe(productId3, List.of(new IngredientAmount(ingredientId3, product3Ingredient3.amountInGrams())));

    @Test
    @DisplayName("Get total ingredient amounts in grams. Getting amounts succeeds.")
    public void getTotalIngredientAmountsInGrams_shouldSucceed() throws Exception {
        List<RequestedOrderItemDetails> requestedItems = List.of(new RequestedOrderItemDetails(productId1, 2),
                new RequestedOrderItemDetails(productId2, 1), new RequestedOrderItemDetails(productId3, 1));
        OrderBase orderBase = new OrderPreparationDetails(UUID.randomUUID(), UUID.randomUUID(), requestedItems);
        List<ProductRecipe> recipes = List.of(productRecipe1, productRecipe2, productRecipe3);
        OrderWithRecipe orderWithRecipe = new OrderPreparationDetails(orderBase, recipes);

        OrderWithConsumption orderWithConsumption = consumptionCalculationService.process(orderWithRecipe);

        assertEquals(3, orderWithConsumption.consumption().size());
        Map<Long, Integer> amountsMap = orderWithConsumption.consumption().stream().collect(Collectors.toMap(IngredientAmount::ingredientId, IngredientAmount::amountInGrams));
        assertEquals(400, amountsMap.get(ingredientId1));
        assertEquals(100, amountsMap.get(ingredientId2));
        assertEquals(150, amountsMap.get(ingredientId3));
    }
}
