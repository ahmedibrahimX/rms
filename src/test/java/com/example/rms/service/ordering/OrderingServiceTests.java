package com.example.rms.service.ordering;

import com.example.rms.infra.entity.*;
import com.example.rms.infra.repo.*;
import com.example.rms.service.OrderPreparationService;
import com.example.rms.service.OrderingService;
import com.example.rms.service.RecipeService;
import com.example.rms.service.StockService;
import com.example.rms.service.model.IngredientAmount;
import com.example.rms.service.model.ProductRecipe;
import com.example.rms.service.model.RequestedProductDetails;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderingServiceTests {
    @Mock
    private ProductIngredientRepo productIngredientRepo;
    @Mock
    private RecipeService recipeService;
    @Mock
    private OrderPreparationService orderPreparationService;
    @Mock
    private StockService stockService;
    @Mock
    private OrderRepo orderRepo;
    @Mock
    private OrderItemRepo orderItemRepo;
    @Captor
    private ArgumentCaptor<Order> orderCaptor;
    @Captor
    private ArgumentCaptor<List<OrderItem>> orderItemCaptor;
    @Captor
    private ArgumentCaptor<UUID> stockBranchIdCaptor;
    @Captor
    private ArgumentCaptor<List<IngredientAmount>> actualTotalAmountInGrams;
    @Captor
    private ArgumentCaptor<Set<Long>> productIdsCaptor;
    @Captor
    private ArgumentCaptor<List<ProductRecipe>> recipesCaptor;
    @Captor
    private ArgumentCaptor<List<RequestedProductDetails>> amountCalculationProductDetailsCaptor;
    @Captor
    private ArgumentCaptor<UUID> orderPlacementBranchIdCaptor;
    @Captor
    private ArgumentCaptor<UUID> orderPlacementCustomerIdCaptor;
    @Captor
    private ArgumentCaptor<List<RequestedProductDetails>> orderPlacementProductDetailsCaptor;

    @InjectMocks
    private OrderingService orderingService;

    private final UUID merchantId1 = UUID.randomUUID();
    private final Merchant merchant1 = new Merchant(merchantId1, "merchant1", "merchant@example.com");
    private final UUID customerId1 = UUID.randomUUID();
    private final UUID branchId1 = UUID.randomUUID();
    private final Branch branch1Merchant1 = new Branch(branchId1, merchantId1, "10", "26 July", "Zamalek", "Cairo", "Egypt");
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

    @Test
    @DisplayName("Happy scenario. Order succeeds, No alerts.")
    public void happyScenario_shouldSucceed() throws Exception {
        ProductRecipe productRecipe1 = new ProductRecipe(productId1, List.of(new IngredientAmount(ingredientId1, product1Ingredient1.amountInGrams()), new IngredientAmount(ingredientId2, product1Ingredient2.amountInGrams())));
        ProductRecipe productRecipe2 = new ProductRecipe(productId2, List.of(new IngredientAmount(ingredientId1, product2Ingredient1.amountInGrams()), new IngredientAmount(ingredientId3, product2Ingredient3.amountInGrams())));
        ProductRecipe productRecipe3 = new ProductRecipe(productId3, List.of(new IngredientAmount(ingredientId3, product3Ingredient3.amountInGrams())));
        List<ProductRecipe> recipes = List.of(productRecipe1, productRecipe2, productRecipe3);
        when(recipeService.getRecipes(any())).thenReturn(recipes);
        List<IngredientAmount> totalAmountsInGrams = List.of(new IngredientAmount(ingredientId1, 400), new IngredientAmount(ingredientId2, 100), new IngredientAmount(ingredientId3, 150));
        when(orderPreparationService.getTotalAmountsInGrams(any(), any())).thenReturn(totalAmountsInGrams);

        List<RequestedProductDetails> requestedProductDetails = List.of(new RequestedProductDetails(productId1, 2),
                new RequestedProductDetails(productId2, 1), new RequestedProductDetails(productId3, 1));
        orderingService.placeOrder(requestedProductDetails, customerId1, branchId1);

        verify(recipeService, times(1)).getRecipes(productIdsCaptor.capture());
        assertTrue(productIdsCaptor.getValue().containsAll(Set.of(productId1, productId2, productId3)));
        verify(orderPreparationService).getTotalAmountsInGrams(recipesCaptor.capture(), amountCalculationProductDetailsCaptor.capture());
        assertEquals(recipes, recipesCaptor.getValue());
        assertEquals(requestedProductDetails, amountCalculationProductDetailsCaptor.getValue());
        verify(stockService, times(1)).consumeIngredients(stockBranchIdCaptor.capture(), actualTotalAmountInGrams.capture());
        assertEquals(branchId1, stockBranchIdCaptor.getValue());
        assertEquals(totalAmountsInGrams, actualTotalAmountInGrams.getValue());
        verify(orderPreparationService, times(1)).place(orderPlacementBranchIdCaptor.capture(), orderPlacementCustomerIdCaptor.capture(), orderPlacementProductDetailsCaptor.capture());
        assertEquals(branchId1, orderPlacementBranchIdCaptor.getValue());
        assertEquals(customerId1, orderPlacementCustomerIdCaptor.getValue());
        assertEquals(requestedProductDetails, orderPlacementProductDetailsCaptor.getValue());
    }

    @Test
    @DisplayName("Invalid product quantities. Should fail with descriptive exception")
    public void invalidQuantity_shouldFailWithDescriptiveException() throws Exception {
        throw new Exception("Not implemented");
    }

    @Test
    @DisplayName("Order product(s) not found under merchant. Should fail with descriptive exception")
    public void productNotFound_shouldFailWithDescriptiveException() throws Exception {
        throw new Exception("Not implemented");
    }
}
