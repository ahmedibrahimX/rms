package com.example.rms.service.ordering;

import com.example.rms.exception.model.StockUpdateFailedException;
import com.example.rms.infra.entity.*;
import com.example.rms.infra.repo.*;
import com.example.rms.service.OrderingService;
import com.example.rms.service.model.RequestedProductDetails;
import jakarta.persistence.OptimisticLockException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.*;

@SpringBootTest
public class StockServiceRaceConditionTest {
    @MockBean
    private ProductIngredientRepo productIngredientRepo;
    @SpyBean
    private IngredientStockRepo ingredientStockRepo;

    @Autowired
    private OrderingService orderingService;

    private final UUID customerId1 = UUID.randomUUID();
    private final UUID branchId1 = UUID.randomUUID();
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
    @DisplayName("Race conditions handling using optimistic locks. Should retry 3 times, if all failed then throw an exception that can be handled gracefully.")
    public void raceConditionHandlingUsingOptimisticLocks_shouldRetryThreeTimesThenThrowCustomExceptionIfAllFail() throws Exception {
        when(productIngredientRepo.findAllByProductIdIn(any())).thenReturn(List.of(product1Ingredient1, product1Ingredient2, product2Ingredient1, product2Ingredient3, product3Ingredient3));
        IngredientStock ingredientStock1 = new IngredientStock(UUID.randomUUID(), branchId1, ingredientId1, BigDecimal.valueOf(Integer.MAX_VALUE), BigDecimal.valueOf(Integer.MAX_VALUE));
        IngredientStock ingredientStock2 = new IngredientStock(UUID.randomUUID(), branchId1, ingredientId2, BigDecimal.valueOf(Integer.MAX_VALUE), BigDecimal.valueOf(Integer.MAX_VALUE));
        IngredientStock ingredientStock3 = new IngredientStock(UUID.randomUUID(), branchId1, ingredientId3, BigDecimal.valueOf(Integer.MAX_VALUE), BigDecimal.valueOf(Integer.MAX_VALUE));
        when(ingredientStockRepo.findByBranchIdAndIngredientIdIn(any(), any())).thenReturn(Set.of(ingredientStock1, ingredientStock2, ingredientStock3));
        when(ingredientStockRepo.saveAll(anyCollection()))
                .thenThrow(new OptimisticLockException())
                .thenThrow(new OptimisticLockException())
                .thenThrow(new OptimisticLockException());

        List<RequestedProductDetails> productRequests = List.of(new RequestedProductDetails(productId1, 2),
                new RequestedProductDetails(productId2, 1), new RequestedProductDetails(productId3, 1));
        assertThrows(StockUpdateFailedException.class, () -> orderingService.placeOrder(productRequests, customerId1, branchId1));

        verify(ingredientStockRepo, times(3)).saveAll(anyCollection());
    }
}
