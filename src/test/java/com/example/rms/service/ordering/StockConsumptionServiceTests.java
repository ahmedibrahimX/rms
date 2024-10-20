package com.example.rms.service.ordering;

import com.example.rms.service.exception.InsufficientIngredientsException;
import com.example.rms.service.exception.StockUpdateFailedException;
import com.example.rms.infra.entity.*;
import com.example.rms.infra.repo.IngredientStockRepo;
import com.example.rms.service.StockConsumptionService;
import com.example.rms.service.model.IngredientAmount;
import com.example.rms.service.model.OrderPreparationDetails;
import com.example.rms.service.model.interfaces.OrderBase;
import com.example.rms.service.model.interfaces.OrderWithConsumption;
import com.example.rms.service.model.interfaces.OrderWithRecipe;
import com.example.rms.service.pattern.decorator.RetriableStepDecorator;
import jakarta.persistence.OptimisticLockException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StockConsumptionServiceTests {
    @Mock
    private IngredientStockRepo ingredientStockRepo;
    @Captor
    private ArgumentCaptor<List<IngredientStock>> ingredientStockCaptor;
    @Captor
    private ArgumentCaptor<UUID> ingredientStockIdCaptor;
    @Captor
    private ArgumentCaptor<BigDecimal> amountCaptor;

    @InjectMocks
    private StockConsumptionService stockConsumptionService;

    private final UUID branchId1 = UUID.randomUUID();
    private final Long ingredientId1 = 1L;
    private final Long ingredientId2 = 2L;
    private final Long ingredientId3 = 3L;

    @Test
    @DisplayName("Happy scenario. Consume ingredients and update stock accurately based on consumed amounts. Stock update succeeds, No alerts.")
    public void happyScenario_processAndUpdateStockAccurately_shouldSucceed() throws Exception {
        IngredientStock ingredientStock1 = new IngredientStock(UUID.randomUUID(), branchId1, ingredientId1, BigDecimal.valueOf(Integer.MAX_VALUE), BigDecimal.valueOf(Integer.MAX_VALUE));
        IngredientStock ingredientStock2 = new IngredientStock(UUID.randomUUID(), branchId1, ingredientId2, BigDecimal.valueOf(Integer.MAX_VALUE), BigDecimal.valueOf(Integer.MAX_VALUE));
        IngredientStock ingredientStock3 = new IngredientStock(UUID.randomUUID(), branchId1, ingredientId3, BigDecimal.valueOf(Integer.MAX_VALUE), BigDecimal.valueOf(Integer.MAX_VALUE));
        when(ingredientStockRepo.findByBranchIdAndIngredientIdIn(any(), any())).thenReturn(Set.of(ingredientStock1, ingredientStock2, ingredientStock3));

        OrderBase orderBase = new OrderPreparationDetails(branchId1, UUID.randomUUID(), new ArrayList<>());
        OrderWithRecipe orderWithRecipe = new OrderPreparationDetails(orderBase, new ArrayList<>());
        List<IngredientAmount> totalConsumptionsInGrams = List.of(new IngredientAmount(ingredientId1, 400), new IngredientAmount(ingredientId2, 100), new IngredientAmount(ingredientId3, 150));
        OrderWithConsumption orderWithConsumption = new OrderPreparationDetails(orderWithRecipe, totalConsumptionsInGrams);
        stockConsumptionService.process(orderWithConsumption);

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
    @DisplayName("Reverting a consumption. Revert succeeds")
    public void revertingConsumption_shouldSucceed() throws Exception {
        IngredientStock ingredientStock1 = new IngredientStock(UUID.randomUUID(), branchId1, ingredientId1, BigDecimal.ZERO, BigDecimal.valueOf(Integer.MAX_VALUE));
        IngredientStock ingredientStock2 = new IngredientStock(UUID.randomUUID(), branchId1, ingredientId2, BigDecimal.ZERO, BigDecimal.valueOf(Integer.MAX_VALUE));
        IngredientStock ingredientStock3 = new IngredientStock(UUID.randomUUID(), branchId1, ingredientId3, BigDecimal.ZERO, BigDecimal.valueOf(Integer.MAX_VALUE));
        when(ingredientStockRepo.findByBranchIdAndIngredientIdIn(any(), any())).thenReturn(Set.of(ingredientStock1, ingredientStock2, ingredientStock3));

        OrderBase orderBase = new OrderPreparationDetails(branchId1, UUID.randomUUID(), new ArrayList<>());
        OrderWithRecipe orderWithRecipe = new OrderPreparationDetails(orderBase, new ArrayList<>());
        List<IngredientAmount> totalConsumptionsInGrams = List.of(new IngredientAmount(ingredientId1, 400), new IngredientAmount(ingredientId2, 100), new IngredientAmount(ingredientId3, 150));
        OrderWithConsumption orderWithConsumption = new OrderPreparationDetails(orderWithRecipe, totalConsumptionsInGrams);
        stockConsumptionService.revert(orderWithConsumption);

        verify(ingredientStockRepo, times(3)).incrementAmountInKilos(ingredientStockIdCaptor.capture(), amountCaptor.capture());
        assertEquals(ingredientStock1.id(), ingredientStockIdCaptor.getAllValues().get(0));
        assertEquals(0.4, amountCaptor.getAllValues().get(0).doubleValue());
        assertEquals(ingredientStock2.id(), ingredientStockIdCaptor.getAllValues().get(1));
        assertEquals(0.1, amountCaptor.getAllValues().get(1).doubleValue());
        assertEquals(ingredientStock3.id(), ingredientStockIdCaptor.getAllValues().get(2));
        assertEquals(0.15, amountCaptor.getAllValues().get(2).doubleValue());
        verifyNoMoreInteractions(ingredientStockRepo);
    }

    @Test
    @DisplayName("Race conditions handling using optimistic locks. Should retry 3 times, if all failed then throw an exception that can be handled gracefully.")
    public void raceConditionHandlingUsingOptimisticLocks_shouldRetryThreeTimesThenThrowCustomExceptionIfAllFail() throws Exception {
        RetriableStepDecorator<OrderWithConsumption, OrderWithConsumption> retriableStockConsumption = new RetriableStepDecorator<>(stockConsumptionService, 3, 1000L, 2);
        IngredientStock ingredientStock1 = new IngredientStock(UUID.randomUUID(), branchId1, ingredientId1, BigDecimal.valueOf(Integer.MAX_VALUE), BigDecimal.valueOf(Integer.MAX_VALUE));
        IngredientStock ingredientStock2 = new IngredientStock(UUID.randomUUID(), branchId1, ingredientId2, BigDecimal.valueOf(Integer.MAX_VALUE), BigDecimal.valueOf(Integer.MAX_VALUE));
        IngredientStock ingredientStock3 = new IngredientStock(UUID.randomUUID(), branchId1, ingredientId3, BigDecimal.valueOf(Integer.MAX_VALUE), BigDecimal.valueOf(Integer.MAX_VALUE));
        when(ingredientStockRepo.findByBranchIdAndIngredientIdIn(any(), any())).thenReturn(Set.of(ingredientStock1, ingredientStock2, ingredientStock3));
        when(ingredientStockRepo.saveAll(anyCollection()))
                .thenThrow(new OptimisticLockException())
                .thenThrow(new OptimisticLockException())
                .thenThrow(new OptimisticLockException());

        OrderBase orderBase = new OrderPreparationDetails(branchId1, UUID.randomUUID(), new ArrayList<>());
        OrderWithRecipe orderWithRecipe = new OrderPreparationDetails(orderBase, new ArrayList<>());
        List<IngredientAmount> totalConsumptionsInGrams = List.of(new IngredientAmount(ingredientId1, 400), new IngredientAmount(ingredientId2, 100), new IngredientAmount(ingredientId3, 150));
        OrderWithConsumption orderWithConsumption = new OrderPreparationDetails(orderWithRecipe, totalConsumptionsInGrams);

        assertThrows(StockUpdateFailedException.class, () -> retriableStockConsumption.process(orderWithConsumption));

        verify(ingredientStockRepo, times(3)).saveAll(anyCollection());
    }

    @Test
    @DisplayName("Insufficient ingredient(s) branch stock. Should fail with descriptive exception")
    public void insufficientIngredients_shouldFailWithDescriptiveException() throws Exception {
        IngredientStock ingredientStock1 = new IngredientStock(UUID.randomUUID(), branchId1, ingredientId1, BigDecimal.valueOf(1), BigDecimal.valueOf(Integer.MAX_VALUE));
        IngredientStock ingredientStock2 = new IngredientStock(UUID.randomUUID(), branchId1, ingredientId2, BigDecimal.valueOf(Integer.MAX_VALUE), BigDecimal.valueOf(Integer.MAX_VALUE));
        IngredientStock ingredientStock3 = new IngredientStock(UUID.randomUUID(), branchId1, ingredientId3, BigDecimal.valueOf(Integer.MAX_VALUE), BigDecimal.valueOf(Integer.MAX_VALUE));
        when(ingredientStockRepo.findByBranchIdAndIngredientIdIn(any(), any())).thenReturn(Set.of(ingredientStock1, ingredientStock2, ingredientStock3));

        OrderBase orderBase = new OrderPreparationDetails(branchId1, UUID.randomUUID(), new ArrayList<>());
        OrderWithRecipe orderWithRecipe = new OrderPreparationDetails(orderBase, new ArrayList<>());
        List<IngredientAmount> totalConsumptionsInGrams = List.of(new IngredientAmount(ingredientId1, 1500), new IngredientAmount(ingredientId2, 100), new IngredientAmount(ingredientId3, 150));
        OrderWithConsumption orderWithConsumption = new OrderPreparationDetails(orderWithRecipe, totalConsumptionsInGrams);
        assertThrows(InsufficientIngredientsException.class, () -> stockConsumptionService.process(orderWithConsumption));


        verifyNoMoreInteractions(ingredientStockRepo);
    }

    @Test
    @DisplayName("Sufficient ingredients for order but ingredient(s) branch stock will hit the threshold for the first time. Should succeed but alert the merchant about those first hits.")
    public void sufficientIngredientsButOneOrMoreIngredientStocksHitThresholdForFirstTime_shouldSucceed_alertMerchant() throws Exception {
        throw new Exception("Not implemented");
    }
}
