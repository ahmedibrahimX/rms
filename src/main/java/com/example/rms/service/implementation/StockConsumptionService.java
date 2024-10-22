package com.example.rms.service.implementation;

import com.example.rms.service.abstraction.OrderPipelineEventHandler;
import com.example.rms.service.abstraction.StockConsumptionStep;
import com.example.rms.service.event.implementation.IngredientStockAlertEvent;
import com.example.rms.service.event.implementation.OrderPlacementRevertedEvent;
import com.example.rms.service.exception.InsufficientIngredientsException;
import com.example.rms.service.exception.StockUpdateFailedException;
import com.example.rms.infra.entity.IngredientStock;
import com.example.rms.infra.repo.IngredientStockRepo;
import com.example.rms.service.model.implementation.NewOrderPreparationDetails;
import com.example.rms.service.model.abstraction.IngredientAmount;
import com.example.rms.service.model.StockAmount;
import com.example.rms.service.model.abstraction.NewOrderWithConsumption;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class StockConsumptionService implements StockConsumptionStep, OrderPipelineEventHandler<OrderPlacementRevertedEvent> {
    private final IngredientStockRepo ingredientStockRepo;
    private final ApplicationEventPublisher eventPublisher;
    private final BigDecimal THRESHOLD;

    @Autowired
    public StockConsumptionService(IngredientStockRepo ingredientStockRepo,
                                   ApplicationEventPublisher eventPublisher,
                                   @Value("${alert.ingredient.threshold:0.5}") double threshold) {
        this.ingredientStockRepo = ingredientStockRepo;
        this.eventPublisher = eventPublisher;
        this.THRESHOLD = BigDecimal.valueOf(threshold);
    }

    public NewOrderWithConsumption process(NewOrderWithConsumption order) {
        Map<Long, Integer> amountsInGramsMap = order.consumption().stream().collect(Collectors.toMap(IngredientAmount::ingredientId, IngredientAmount::amountInGrams));
        Set<Long> ingredientIds = amountsInGramsMap.keySet();
        Map<Long, IngredientStock> currentStocks = ingredientStockRepo.findByBranchIdAndIngredientIdIn(order.branchId(), ingredientIds).stream()
                .collect(Collectors.toMap(IngredientStock::ingredientId, stock -> stock));

        List<IngredientStock> updatedStocks = new ArrayList<>();
        List<StockAmount> stocksAmountsHittingThreshold = new ArrayList<>();
        for (var currentStock : currentStocks.entrySet()) {
            Long ingredientId = currentStock.getKey();
            IngredientStock updated = new IngredientStock(currentStock.getValue());
            BigDecimal previousAmountInKilos = updated.amountInKilos();
            BigDecimal consumedAmountInKilos = BigDecimal.valueOf(amountsInGramsMap.get(ingredientId))
                    .divide(BigDecimal.valueOf(1000), 3, RoundingMode.HALF_UP);
            BigDecimal updatedAmountInKilos = previousAmountInKilos.subtract(consumedAmountInKilos);

            if (isInsufficientStock(updatedAmountInKilos)) {
                throw new InsufficientIngredientsException();
            }

            BigDecimal criticalCapacity = currentStock.getValue().maxCapacityInKilos().multiply(THRESHOLD);
            if (greaterThan(previousAmountInKilos, criticalCapacity) && lessThanOrEqual(updatedAmountInKilos, criticalCapacity)) {
                stocksAmountsHittingThreshold.add(new StockAmount(ingredientId, updatedAmountInKilos));
            }

            updated.amountInKilos(updatedAmountInKilos);
            updatedStocks.add(updated);
        }
        try {
            ingredientStockRepo.saveAll(updatedStocks);
            if (!stocksAmountsHittingThreshold.isEmpty()) {
                eventPublisher.publishEvent(new IngredientStockAlertEvent(this, order.branchId(), stocksAmountsHittingThreshold));
            }
            return new NewOrderPreparationDetails(order);
        } catch (Exception ex) {
            log.error("Not able to update stock. An exception was thrown {}", ex.toString());
            throw new StockUpdateFailedException(ex);
        }
    }

    private static boolean greaterThan(BigDecimal previousAmountInKilos, BigDecimal maxCapacity) {
        return previousAmountInKilos.compareTo(maxCapacity) > 0;
    }

    private static boolean lessThanOrEqual(BigDecimal updatedAmountInKilos, BigDecimal maxCapacity) {
        return updatedAmountInKilos.compareTo(maxCapacity) <= 0;
    }

    @Async
    @EventListener
    public void handle(OrderPlacementRevertedEvent event) {
        revert(event.order());
    }

    public void revert(NewOrderWithConsumption order) {
        Map<Long, Integer> amountsInGramsMap = order.consumption().stream().collect(Collectors.toMap(IngredientAmount::ingredientId, IngredientAmount::amountInGrams));
        Set<Long> ingredientIds = amountsInGramsMap.keySet();
        Map<Long, IngredientStock> currentStocks = ingredientStockRepo.findByBranchIdAndIngredientIdIn(order.branchId(), ingredientIds).stream()
                .collect(Collectors.toMap(IngredientStock::ingredientId, stock -> stock));

        for (var currentStock : currentStocks.entrySet()) {
            Long ingredientId = currentStock.getKey();
            BigDecimal consumedAmountInKilos = BigDecimal.valueOf(amountsInGramsMap.get(ingredientId))
                    .divide(BigDecimal.valueOf(1000), 3, RoundingMode.HALF_UP);
            ingredientStockRepo.incrementAmountInKilos(currentStock.getValue().id(), consumedAmountInKilos);
        }
    }

    private static boolean isInsufficientStock(BigDecimal updatedAmountInKilos) {
        return updatedAmountInKilos.compareTo(BigDecimal.ZERO) < 0;
    }
}
