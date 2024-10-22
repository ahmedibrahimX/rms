package com.example.rms.service.ordering;

import com.example.rms.service.exception.StockUpdateFailedException;
import com.example.rms.infra.entity.*;
import com.example.rms.service.implementation.*;
import com.example.rms.service.model.abstraction.*;
import com.example.rms.service.model.implementation.*;
import jakarta.persistence.OptimisticLockException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderingPipelineServiceTests {
    @Mock
    private OrderValidationService orderValidationService;
    @Mock
    private RecipeService recipeService;
    @Mock
    private ConsumptionCalculationService consumptionCalculationService;
    @Mock
    private OrderPlacementService orderPlacementService;
    @Mock
    private StockConsumptionService stockConsumptionService;
    @Captor
    private ArgumentCaptor<NewOrderPreparationDetails> validationStepParamCaptor;
    @Captor
    private ArgumentCaptor<NewOrderPreparationDetails> recipeStepParamCaptor;
    @Captor
    private ArgumentCaptor<NewOrderWithRecipe> consumptionCalculationStepParamCaptor;
    @Captor
    private ArgumentCaptor<NewOrderWithConsumption> stockConsumptionStepParamCaptor;
    @Captor
    private ArgumentCaptor<NewOrderWithConsumption> orderPlacementParamCaptor;

    private OrderingPipelineService orderingPipelineService;

    private final UUID merchantId1 = UUID.randomUUID();
    private final Merchant merchant1 = new Merchant(merchantId1, "merchant1", "merchant@example.com");
    private final UUID customerId1 = UUID.randomUUID();
    private final UUID branchId1 = UUID.randomUUID();
    private final Branch branch1Merchant1 = new Branch(branchId1, merchantId1, 10, "26 July", "Zamalek", "Cairo", "Egypt");
    private final Long ingredientId1 = 1L;
    private final Ingredient ingredient1 = new Ingredient(ingredientId1, "ingredient1");
    private final Long ingredientId2 = 2L;
    private final Ingredient ingredient2 = new Ingredient(ingredientId2, "ingredient2");
    private final Long ingredientId3 = 3L;
    private final Ingredient ingredient3 = new Ingredient(ingredientId3, "ingredient2");
    private final Long productId1 = 1L;
    private final Product product1 = new Product(productId1, merchantId1, "product1");
    private final UUID product1Ingredient1Id = UUID.randomUUID();
    private final ProductIngredient product1Ingredient1 = new ProductIngredient(product1Ingredient1Id, productId1, ingredientId1, 100);
    private final UUID product1Ingredient2Id = UUID.randomUUID();
    private final ProductIngredient product1Ingredient2 = new ProductIngredient(product1Ingredient2Id, productId1, ingredientId2, 50);
    private final Long productId2 = 2L;
    private final Product product2 = new Product(productId2, merchantId1, "product2");
    private final UUID product2Ingredient1Id = UUID.randomUUID();
    private final ProductIngredient product2Ingredient1 = new ProductIngredient(product2Ingredient1Id, productId2, ingredientId1, 200);
    private final UUID product2Ingredient3Id = UUID.randomUUID();
    private final ProductIngredient product2Ingredient3 = new ProductIngredient(product2Ingredient3Id, productId2, ingredientId3, 100);
    private final Long productId3 = 3L;
    private final Product product3 = new Product(productId3, merchantId1, "product3");
    private final UUID product3Ingredient3Id = UUID.randomUUID();
    private final ProductIngredient product3Ingredient3 = new ProductIngredient(product3Ingredient3Id, productId3, ingredientId3, 50);
    private final ProductRecipe productRecipe1 = new ProductRecipe(productId1, List.of(new RecipeIngredientAmount(ingredientId1, product1Ingredient1.amountInGrams()), new RecipeIngredientAmount(ingredientId2, product1Ingredient2.amountInGrams())));
    private final ProductRecipe productRecipe2 = new ProductRecipe(productId2, List.of(new RecipeIngredientAmount(ingredientId1, product2Ingredient1.amountInGrams()), new RecipeIngredientAmount(ingredientId3, product2Ingredient3.amountInGrams())));
    private final ProductRecipe productRecipe3 = new ProductRecipe(productId3, List.of(new RecipeIngredientAmount(ingredientId3, product3Ingredient3.amountInGrams())));

    @BeforeEach
    public void setup() {
        orderingPipelineService = new OrderingPipelineService(orderValidationService, recipeService, consumptionCalculationService, stockConsumptionService, orderPlacementService, 3, 1000L, 2);
    }

    @Test
    @DisplayName("Asserting pipeline flow for customer requesting an order")
    public void assertingPipelineFlow() throws Exception {
        List<RequestedOrderItemDetails> requestedItems = List.of(new RequestedOrderItemDetails(productId1, 2),
                new RequestedOrderItemDetails(productId2, 1), new RequestedOrderItemDetails(productId3, 1));
        NewOrder order = new NewOrderPreparationDetails(branchId1, customerId1, requestedItems);
        when(orderValidationService.process(any())).thenReturn(order);
        List<ProductRecipe> recipes = List.of(productRecipe1, productRecipe2, productRecipe3);
        NewOrderWithRecipe newOrderWithRecipes = new NewOrderPreparationDetails(order, recipes);
        when(recipeService.process(any())).thenReturn(newOrderWithRecipes);
        List<ConsumptionIngredientAmount> totalAmountsInGrams = List.of(new ConsumptionIngredientAmount(ingredientId1, 400),
                new ConsumptionIngredientAmount(ingredientId2, 100),
                new ConsumptionIngredientAmount(ingredientId3, 150));
        NewOrderWithConsumption newOrderWithConsumption = new NewOrderPreparationDetails(newOrderWithRecipes, totalAmountsInGrams);
        when(consumptionCalculationService.process(any())).thenReturn(newOrderWithConsumption);
        when(stockConsumptionService.process(any())).thenReturn(newOrderWithConsumption);

        orderingPipelineService.placeOrder(order);

        verify(orderValidationService, times(1)).process(validationStepParamCaptor.capture());
        assertEquals(order, validationStepParamCaptor.getValue());
        verify(recipeService, times(1)).process(recipeStepParamCaptor.capture());
        assertEquals(order, recipeStepParamCaptor.getValue());
        verify(consumptionCalculationService).process(consumptionCalculationStepParamCaptor.capture());
        assertEquals(newOrderWithRecipes, consumptionCalculationStepParamCaptor.getValue());
        verify(stockConsumptionService, times(1)).process(stockConsumptionStepParamCaptor.capture());
        assertEquals(newOrderWithConsumption, stockConsumptionStepParamCaptor.getValue());
        verify(orderPlacementService, times(1)).process(orderPlacementParamCaptor.capture());
        assertEquals(newOrderWithConsumption, orderPlacementParamCaptor.getValue());
    }

    @Test
    @DisplayName("Testing retrial mechanism in pipeline level.")
    public void raceConditionHandlingInPipelineLevel_shouldRetryThreeTimesThenThrowIfAllRetrialsFail() throws Exception {
        List<RequestedOrderItemDetails> requestedItems = List.of(new RequestedOrderItemDetails(productId1, 2),
                new RequestedOrderItemDetails(productId2, 1), new RequestedOrderItemDetails(productId3, 1));
        NewOrder order = new NewOrderPreparationDetails(branchId1, customerId1, requestedItems);
        when(orderValidationService.process(any())).thenReturn(order);
        List<ProductRecipe> recipes = List.of(productRecipe1, productRecipe2, productRecipe3);
        NewOrderWithRecipe newOrderWithRecipes = new NewOrderPreparationDetails(order, recipes);
        when(recipeService.process(any())).thenReturn(newOrderWithRecipes);
        List<ConsumptionIngredientAmount> totalAmountsInGrams = List.of(new ConsumptionIngredientAmount(ingredientId1, 400), new ConsumptionIngredientAmount(ingredientId2, 100), new ConsumptionIngredientAmount(ingredientId3, 150));
        NewOrderWithConsumption newOrderWithConsumption = new NewOrderPreparationDetails(newOrderWithRecipes, totalAmountsInGrams);
        when(consumptionCalculationService.process(any())).thenReturn(newOrderWithConsumption);
        when(stockConsumptionService.process(any())).thenThrow(new StockUpdateFailedException(new OptimisticLockException()));

        assertThrows(StockUpdateFailedException.class, () -> orderingPipelineService.placeOrder(order));
    }
}
