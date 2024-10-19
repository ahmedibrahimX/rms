package com.example.rms.service.ordering;

import com.example.rms.infra.entity.*;
import com.example.rms.infra.repo.IngredientStockRepo;
import com.example.rms.infra.repo.OrderItemRepo;
import com.example.rms.infra.repo.OrderRepo;
import com.example.rms.infra.repo.ProductIngredientRepo;
import com.example.rms.service.OrderingService;
import com.example.rms.service.StockService;
import com.example.rms.service.model.RequestedProductDetails;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StockServiceTests {
    @Mock
    private IngredientStockRepo ingredientStockRepo;
    @Captor
    private ArgumentCaptor<List<IngredientStock>> ingredientStockCaptor;

    @InjectMocks
    private StockService stockService;

    private final UUID branchId1 = UUID.randomUUID();
    private final Long ingredientId1 = 1L;
    private final Long ingredientId2 = 2L;
    private final Long ingredientId3 = 3L;

    @Test
    @DisplayName("Happy scenario. Update ingredient stocks accurately based on consumed amounts. Stock update succeeds, No alerts.")
    public void happyScenario_updateIngredientsStockAccurately_shouldSucceed() throws Exception {
        IngredientStock ingredientStock1 = new IngredientStock(UUID.randomUUID(), branchId1, ingredientId1, BigDecimal.valueOf(Integer.MAX_VALUE), BigDecimal.valueOf(Integer.MAX_VALUE));
        IngredientStock ingredientStock2 = new IngredientStock(UUID.randomUUID(), branchId1, ingredientId2, BigDecimal.valueOf(Integer.MAX_VALUE), BigDecimal.valueOf(Integer.MAX_VALUE));
        IngredientStock ingredientStock3 = new IngredientStock(UUID.randomUUID(), branchId1, ingredientId3, BigDecimal.valueOf(Integer.MAX_VALUE), BigDecimal.valueOf(Integer.MAX_VALUE));
        when(ingredientStockRepo.findByBranchIdAndIngredientIdIn(any(), any())).thenReturn(Set.of(ingredientStock1, ingredientStock2, ingredientStock3));

        Map<Long, Integer> consumedIngredientAmountInGrams = new HashMap<>();
        consumedIngredientAmountInGrams.put(1L, 400);
        consumedIngredientAmountInGrams.put(2L, 100);
        consumedIngredientAmountInGrams.put(3L, 150);
        stockService.updateStock(branchId1, consumedIngredientAmountInGrams);

        verify(ingredientStockRepo, times(1)).saveAll(ingredientStockCaptor.capture());
        Map<UUID, IngredientStock> updatedStock = ingredientStockCaptor.getValue().stream().collect(Collectors.toMap(IngredientStock::id, s -> s));
        BigDecimal expectedValue1 = BigDecimal.valueOf(Integer.MAX_VALUE).subtract(BigDecimal.valueOf(100 * 2 + 200).divide(BigDecimal.valueOf(1000), 3, RoundingMode.HALF_UP));
        assertEquals(expectedValue1, updatedStock.get(ingredientStock1.id()).amountInKilos());
        BigDecimal expectedValue2 = BigDecimal.valueOf(Integer.MAX_VALUE).subtract(BigDecimal.valueOf(50 * 2).divide(BigDecimal.valueOf(1000), 3, RoundingMode.HALF_UP));
        assertEquals(expectedValue2, updatedStock.get(ingredientStock2.id()).amountInKilos());
        BigDecimal expectedValue3 = BigDecimal.valueOf(Integer.MAX_VALUE).subtract(BigDecimal.valueOf(100 + 50).divide(BigDecimal.valueOf(1000), 3, RoundingMode.HALF_UP));
        assertEquals(expectedValue3, updatedStock.get(ingredientStock3.id()).amountInKilos());
    }

    @Test
    @DisplayName("Product(s) with insufficient ingredient(s) branch stock. Should fail with descriptive exception")
    public void insufficientIngredients_shouldFailWithDescriptiveException() throws Exception {
        throw new Exception("Not implemented");
    }

    @Test
    @DisplayName("Sufficient ingredients for order but ingredient(s) branch stock will hit the threshold for the first time. Should succeed but alert the merchant about those first hits.")
    public void sufficientIngredientsButOneOrMoreIngredientStocksHitThresholdForFirstTime_shouldSucceed_alertMerchant() throws Exception {
        throw new Exception("Not implemented");
    }
}
