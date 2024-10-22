package com.example.rms.service.ordering;

import com.example.rms.service.event.implementation.IngredientStockAlertEvent;
import com.example.rms.service.exception.InsufficientIngredientsException;
import com.example.rms.service.exception.StockUpdateFailedException;
import com.example.rms.infra.entity.*;
import com.example.rms.infra.repo.IngredientStockRepo;
import com.example.rms.service.implementation.StockConsumptionService;
import com.example.rms.service.model.implementation.NewOrderPreparationDetails;
import com.example.rms.service.model.StockAmount;
import com.example.rms.service.model.abstraction.OrderBase;
import com.example.rms.service.model.abstraction.NewOrderWithConsumption;
import com.example.rms.service.model.abstraction.NewOrderWithRecipe;
import com.example.rms.service.model.implementation.ConsumptionIngredientAmount;
import com.example.rms.service.pattern.decorator.RetriableStepDecorator;
import jakarta.persistence.OptimisticLockException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

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
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Captor
    private ArgumentCaptor<List<IngredientStock>> ingredientStockCaptor;
    @Captor
    private ArgumentCaptor<UUID> ingredientStockIdCaptor;
    @Captor
    private ArgumentCaptor<BigDecimal> amountCaptor;
    @Captor
    private ArgumentCaptor<IngredientStockAlertEvent> eventCaptor;

    private StockConsumptionService stockConsumptionService;

    private final UUID branchId1 = UUID.randomUUID();
    private final Long ingredientId1 = 1L;
    private final Long ingredientId2 = 2L;
    private final Long ingredientId3 = 3L;

    @BeforeEach
    public void setup() {
        stockConsumptionService = new StockConsumptionService(ingredientStockRepo, eventPublisher, 0.5);
    }

    @Test
    @DisplayName("Happy scenario. Consume ingredients and update stock accurately based on consumed amounts. Stock update succeeds, No alerts.")
    public void happyScenario_processAndUpdateStockAccurately_shouldSucceed() throws Exception {
        IngredientStock ingredientStock1 = new IngredientStock(UUID.randomUUID(), branchId1, ingredientId1, BigDecimal.valueOf(Integer.MAX_VALUE), BigDecimal.valueOf(Integer.MAX_VALUE));
        IngredientStock ingredientStock2 = new IngredientStock(UUID.randomUUID(), branchId1, ingredientId2, BigDecimal.valueOf(Integer.MAX_VALUE), BigDecimal.valueOf(Integer.MAX_VALUE));
        IngredientStock ingredientStock3 = new IngredientStock(UUID.randomUUID(), branchId1, ingredientId3, BigDecimal.valueOf(Integer.MAX_VALUE), BigDecimal.valueOf(Integer.MAX_VALUE));
        when(ingredientStockRepo.findByBranchIdAndIngredientIdIn(any(), any())).thenReturn(Set.of(ingredientStock1, ingredientStock2, ingredientStock3));

        OrderBase orderBase = new NewOrderPreparationDetails(branchId1, UUID.randomUUID(), new ArrayList<>());
        NewOrderWithRecipe newOrderWithRecipe = new NewOrderPreparationDetails(orderBase, new ArrayList<>());
        List<ConsumptionIngredientAmount> totalConsumptionsInGrams = List.of(new ConsumptionIngredientAmount(ingredientId1, 400),
                new ConsumptionIngredientAmount(ingredientId2, 100),
                new ConsumptionIngredientAmount(ingredientId3, 150));
        NewOrderWithConsumption newOrderWithConsumption = new NewOrderPreparationDetails(newOrderWithRecipe, totalConsumptionsInGrams);
        stockConsumptionService.process(newOrderWithConsumption);

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

        OrderBase orderBase = new NewOrderPreparationDetails(branchId1, UUID.randomUUID(), new ArrayList<>());
        NewOrderWithRecipe newOrderWithRecipe = new NewOrderPreparationDetails(orderBase, new ArrayList<>());
        List<ConsumptionIngredientAmount> totalConsumptionsInGrams = List.of(new ConsumptionIngredientAmount(ingredientId1, 400),
                new ConsumptionIngredientAmount(ingredientId2, 100),
                new ConsumptionIngredientAmount(ingredientId3, 150));
        NewOrderWithConsumption newOrderWithConsumption = new NewOrderPreparationDetails(newOrderWithRecipe, totalConsumptionsInGrams);
        stockConsumptionService.revert(newOrderWithConsumption);

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
        RetriableStepDecorator<NewOrderWithConsumption, NewOrderWithConsumption> retriableStockConsumption = new RetriableStepDecorator<>(stockConsumptionService, 3, 1000L, 2, StockUpdateFailedException.class);
        IngredientStock ingredientStock1 = new IngredientStock(UUID.randomUUID(), branchId1, ingredientId1, BigDecimal.valueOf(Integer.MAX_VALUE), BigDecimal.valueOf(Integer.MAX_VALUE));
        IngredientStock ingredientStock2 = new IngredientStock(UUID.randomUUID(), branchId1, ingredientId2, BigDecimal.valueOf(Integer.MAX_VALUE), BigDecimal.valueOf(Integer.MAX_VALUE));
        IngredientStock ingredientStock3 = new IngredientStock(UUID.randomUUID(), branchId1, ingredientId3, BigDecimal.valueOf(Integer.MAX_VALUE), BigDecimal.valueOf(Integer.MAX_VALUE));
        when(ingredientStockRepo.findByBranchIdAndIngredientIdIn(any(), any())).thenReturn(Set.of(ingredientStock1, ingredientStock2, ingredientStock3));
        when(ingredientStockRepo.saveAll(anyCollection()))
                .thenThrow(new OptimisticLockException())
                .thenThrow(new OptimisticLockException())
                .thenThrow(new OptimisticLockException());

        OrderBase orderBase = new NewOrderPreparationDetails(branchId1, UUID.randomUUID(), new ArrayList<>());
        NewOrderWithRecipe newOrderWithRecipe = new NewOrderPreparationDetails(orderBase, new ArrayList<>());
        List<ConsumptionIngredientAmount> totalConsumptionsInGrams = List.of(new ConsumptionIngredientAmount(ingredientId1, 400),
                new ConsumptionIngredientAmount(ingredientId2, 100),
                new ConsumptionIngredientAmount(ingredientId3, 150));
        NewOrderWithConsumption newOrderWithConsumption = new NewOrderPreparationDetails(newOrderWithRecipe, totalConsumptionsInGrams);

        assertThrows(StockUpdateFailedException.class, () -> retriableStockConsumption.process(newOrderWithConsumption));

        verify(ingredientStockRepo, times(3)).saveAll(anyCollection());
    }

    @Test
    @DisplayName("Insufficient ingredient(s) branch stock. Should fail with descriptive exception")
    public void insufficientIngredients_shouldFailWithDescriptiveException() throws Exception {
        IngredientStock ingredientStock1 = new IngredientStock(UUID.randomUUID(), branchId1, ingredientId1, BigDecimal.valueOf(1), BigDecimal.valueOf(Integer.MAX_VALUE));
        IngredientStock ingredientStock2 = new IngredientStock(UUID.randomUUID(), branchId1, ingredientId2, BigDecimal.valueOf(Integer.MAX_VALUE), BigDecimal.valueOf(Integer.MAX_VALUE));
        IngredientStock ingredientStock3 = new IngredientStock(UUID.randomUUID(), branchId1, ingredientId3, BigDecimal.valueOf(Integer.MAX_VALUE), BigDecimal.valueOf(Integer.MAX_VALUE));
        when(ingredientStockRepo.findByBranchIdAndIngredientIdIn(any(), any())).thenReturn(Set.of(ingredientStock1, ingredientStock2, ingredientStock3));

        OrderBase orderBase = new NewOrderPreparationDetails(branchId1, UUID.randomUUID(), new ArrayList<>());
        NewOrderWithRecipe newOrderWithRecipe = new NewOrderPreparationDetails(orderBase, new ArrayList<>());
        List<ConsumptionIngredientAmount> totalConsumptionsInGrams = List.of(new ConsumptionIngredientAmount(ingredientId1,
                1500), new ConsumptionIngredientAmount(ingredientId2, 100), new ConsumptionIngredientAmount(ingredientId3, 150));
        NewOrderWithConsumption newOrderWithConsumption = new NewOrderPreparationDetails(newOrderWithRecipe, totalConsumptionsInGrams);
        assertThrows(InsufficientIngredientsException.class, () -> stockConsumptionService.process(newOrderWithConsumption));


        verifyNoMoreInteractions(ingredientStockRepo);
    }

    @Test
    @DisplayName("Ingredient stock is sufficient but hits the threshold for the first time. Should succeed but alert the merchant about those first hits.")
    public void sufficientIngredientStockButHitsTheThresholdForTheFirstTime_shouldSucceed_alertMerchant() throws Exception {
        RetriableStepDecorator<NewOrderWithConsumption, NewOrderWithConsumption> retriableStockConsumption = new RetriableStepDecorator<>(stockConsumptionService, 3, 1000L, 2, StockUpdateFailedException.class);
        IngredientStock ingredientStock1 = new IngredientStock(UUID.randomUUID(), branchId1, ingredientId1, BigDecimal.valueOf(5.4), BigDecimal.valueOf(10));
        IngredientStock ingredientStock2 = new IngredientStock(UUID.randomUUID(), branchId1, ingredientId2, BigDecimal.valueOf(2.4), BigDecimal.valueOf(5));
        IngredientStock ingredientStock3 = new IngredientStock(UUID.randomUUID(), branchId1, ingredientId3, BigDecimal.valueOf(3.05), BigDecimal.valueOf(6));
        when(ingredientStockRepo.findByBranchIdAndIngredientIdIn(any(), any())).thenReturn(Set.of(ingredientStock1, ingredientStock2, ingredientStock3));

        OrderBase orderBase = new NewOrderPreparationDetails(branchId1, UUID.randomUUID(), new ArrayList<>());
        NewOrderWithRecipe newOrderWithRecipe = new NewOrderPreparationDetails(orderBase, new ArrayList<>());
        List<ConsumptionIngredientAmount> totalConsumptionsInGrams = List.of(new ConsumptionIngredientAmount(ingredientId1, 400), new ConsumptionIngredientAmount(ingredientId2,
                100), new ConsumptionIngredientAmount(ingredientId3, 150));
        NewOrderWithConsumption newOrderWithConsumption = new NewOrderPreparationDetails(newOrderWithRecipe, totalConsumptionsInGrams);
        stockConsumptionService.process(newOrderWithConsumption);

        verify(ingredientStockRepo, times(1)).saveAll(ingredientStockCaptor.capture());
        Map<UUID, IngredientStock> updatedStock = ingredientStockCaptor.getValue().stream().collect(Collectors.toMap(IngredientStock::id, s -> s));
        assertEquals(5.0, updatedStock.get(ingredientStock1.id()).amountInKilos().doubleValue());
        assertEquals(2.3, updatedStock.get(ingredientStock2.id()).amountInKilos().doubleValue());
        assertEquals(2.9, updatedStock.get(ingredientStock3.id()).amountInKilos().doubleValue());
        verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());
        assertEquals(branchId1, eventCaptor.getValue().branchId());
        assertEquals(2, eventCaptor.getValue().stockAmounts().size());
        Map<Long, StockAmount> stockAmountsHittingThreshold = eventCaptor.getValue().stockAmounts().stream().collect(Collectors.toMap(StockAmount::ingredientId, s -> s));
        assertEquals(5.0, stockAmountsHittingThreshold.get(ingredientId1).amountInKilos().doubleValue());
        assertEquals(2.9, stockAmountsHittingThreshold.get(ingredientId3).amountInKilos().doubleValue());
    }
}
