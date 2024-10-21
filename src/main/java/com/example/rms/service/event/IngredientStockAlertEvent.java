package com.example.rms.service.event;

import com.example.rms.service.model.StockAmount;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.springframework.context.ApplicationEvent;

import java.util.List;
import java.util.UUID;

@Getter
@Accessors(fluent = true)
public class IngredientStockAlertEvent extends ApplicationEvent {
    UUID branchId;
    List<StockAmount> stockAmounts;

    public IngredientStockAlertEvent(Object source, UUID branchId, List<StockAmount> stockAmounts) {
        super(source);
        this.branchId = branchId;
        this.stockAmounts = stockAmounts;
    }
}
