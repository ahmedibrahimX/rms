package com.example.rms.service;

import com.example.rms.service.event.OrderPlacementRevertedEvent;
import com.example.rms.service.exception.InsufficientIngredientsException;
import com.example.rms.service.exception.StockUpdateFailedException;
import com.example.rms.infra.entity.IngredientStock;
import com.example.rms.infra.repo.IngredientStockRepo;
import com.example.rms.service.model.OrderPreparationDetails;
import com.example.rms.service.model.IngredientAmount;
import com.example.rms.service.model.interfaces.OrderWithConsumption;
import com.example.rms.service.pattern.pipeline.Step;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class StockConsumptionService implements Step<OrderWithConsumption, OrderWithConsumption> {
    private final IngredientStockRepo ingredientStockRepo;

    @Autowired
    public StockConsumptionService(IngredientStockRepo ingredientStockRepo) {
        this.ingredientStockRepo = ingredientStockRepo;
    }

    public OrderWithConsumption process(OrderWithConsumption order) {
        Map<Long, Integer> amountsInGramsMap = order.consumption().stream().collect(Collectors.toMap(IngredientAmount::ingredientId, IngredientAmount::amountInGrams));
        Set<Long> ingredientIds = amountsInGramsMap.keySet();
        Map<Long, IngredientStock> currentStocks = ingredientStockRepo.findByBranchIdAndIngredientIdIn(order.branchId(), ingredientIds).stream()
                .collect(Collectors.toMap(IngredientStock::ingredientId, stock -> stock));

        List<IngredientStock> updatedStocks = new ArrayList<>();
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

            updated.amountInKilos(updatedAmountInKilos);
            updatedStocks.add(updated);
        }
        try {
            ingredientStockRepo.saveAll(updatedStocks);
            return new OrderPreparationDetails(order);
        } catch (Exception ex) {
            log.error("Not able to update stock. An exception was thrown {}", ex.toString());
            throw new StockUpdateFailedException(ex);
        }
    }

    @Async
    @EventListener
    public void OrderPlacementRevertHandler(OrderPlacementRevertedEvent event) {
        revert(event.orderWithConsumption());
    }

    public void revert(OrderWithConsumption order) {
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
