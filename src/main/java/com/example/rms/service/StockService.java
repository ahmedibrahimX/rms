package com.example.rms.service;

import com.example.rms.exception.model.StockUpdateFailedException;
import com.example.rms.infra.entity.IngredientStock;
import com.example.rms.infra.repo.IngredientStockRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class StockService {
    private final IngredientStockRepo ingredientStockRepo;

    @Autowired
    public StockService(IngredientStockRepo ingredientStockRepo) {
        this.ingredientStockRepo = ingredientStockRepo;
    }

    @Retryable(value = StockUpdateFailedException.class, maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
    public void consumeIngredients(UUID branchId, Map<Long, Integer> consumedIngredientAmountInGrams) {
        Set<Long> ingredientIds = consumedIngredientAmountInGrams.keySet();
        Map<Long, IngredientStock> currentStocks = ingredientStockRepo.findByBranchIdAndIngredientIdIn(branchId, ingredientIds).stream()
                .collect(Collectors.toMap(IngredientStock::ingredientId, stock -> stock));

        List<IngredientStock> updatedStocks = new ArrayList<>();
        for (var currentStock : currentStocks.entrySet()) {
            Long ingredientId = currentStock.getKey();
            IngredientStock updated = new IngredientStock(currentStock.getValue());
            BigDecimal previousAmountInKilos = updated.amountInKilos();
            BigDecimal consumedAmountInKilos = BigDecimal.valueOf(consumedIngredientAmountInGrams.get(ingredientId))
                    .divide(BigDecimal.valueOf(1000), 3, RoundingMode.HALF_UP);
            BigDecimal updatedAmountInKilos = previousAmountInKilos.subtract(consumedAmountInKilos);
            updated.amountInKilos(updatedAmountInKilos);
            updatedStocks.add(updated);
        }
        try {
            ingredientStockRepo.saveAll(updatedStocks);
        } catch (Exception ex) {
            log.error("Not able to update stock. An exception was thrown {}", ex.toString());
            throw new StockUpdateFailedException();
        }
    }

    @Recover
    public void recover(StockUpdateFailedException ex) {
        log.info("Recovery method called after all stock update retrials exhausted.");
        // Can add any other logic here such as alerts for example
        throw new StockUpdateFailedException();
    }
}
