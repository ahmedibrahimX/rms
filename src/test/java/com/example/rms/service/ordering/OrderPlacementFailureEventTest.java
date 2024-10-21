package com.example.rms.service.ordering;

import com.example.rms.infra.entity.IngredientStock;
import com.example.rms.infra.repo.IngredientStockRepo;
import com.example.rms.service.event.OrderPlacementRevertedEvent;
import com.example.rms.service.model.IngredientAmount;
import com.example.rms.service.model.OrderPreparationDetails;
import com.example.rms.service.model.interfaces.OrderWithConsumption;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles(profiles = {"non-async"})
public class OrderPlacementFailureEventTest {
    @MockBean
    IngredientStockRepo ingredientStockRepo;
    @Captor
    ArgumentCaptor<UUID> stockIdCaptor;
    @Captor
    ArgumentCaptor<BigDecimal> amountCaptor;

    @Autowired
    ApplicationEventPublisher eventPublisher;

    @Test
    public void simulateOrderPlacementFailure_testStockChangesRevert() {
        UUID branchId1 = UUID.randomUUID();
        Long ingredientId1 = 1L;
        Long ingredientId2 = 2L;
        Long ingredientId3 = 3L;
        UUID stockId1 = UUID.randomUUID();
        UUID stockId2 = UUID.randomUUID();
        UUID stockId3 = UUID.randomUUID();
        IngredientStock ingredientStock1 = new IngredientStock(stockId1, branchId1, ingredientId1, BigDecimal.valueOf(Integer.MAX_VALUE), BigDecimal.valueOf(Integer.MAX_VALUE));
        IngredientStock ingredientStock2 = new IngredientStock(stockId2, branchId1, ingredientId2, BigDecimal.valueOf(Integer.MAX_VALUE), BigDecimal.valueOf(Integer.MAX_VALUE));
        IngredientStock ingredientStock3 = new IngredientStock(stockId3, branchId1, ingredientId3, BigDecimal.valueOf(Integer.MAX_VALUE), BigDecimal.valueOf(Integer.MAX_VALUE));
        when(ingredientStockRepo.findByBranchIdAndIngredientIdIn(any(), any())).thenReturn(Set.of(ingredientStock1, ingredientStock2, ingredientStock3));

        List<IngredientAmount> totalConsumptionsInGrams = List.of(new IngredientAmount(ingredientId1, 400), new IngredientAmount(ingredientId2, 100), new IngredientAmount(ingredientId3, 150));
        OrderWithConsumption orderWithConsumption = new OrderPreparationDetails(branchId1, UUID.randomUUID(), new ArrayList<>(), new ArrayList<>(), totalConsumptionsInGrams);
        eventPublisher.publishEvent(new OrderPlacementRevertedEvent(this, orderWithConsumption));

        verify(ingredientStockRepo, times(3)).incrementAmountInKilos(stockIdCaptor.capture(), amountCaptor.capture());
        assertEquals(stockId1, stockIdCaptor.getAllValues().get(0));
        assertEquals(0.4, amountCaptor.getAllValues().get(0).doubleValue());
        assertEquals(stockId2, stockIdCaptor.getAllValues().get(1));
        assertEquals(0.1, amountCaptor.getAllValues().get(1).doubleValue());
        assertEquals(stockId3, stockIdCaptor.getAllValues().get(2));
        assertEquals(0.15, amountCaptor.getAllValues().get(2).doubleValue());
    }
}
