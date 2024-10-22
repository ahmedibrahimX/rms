package com.example.rms.service.abstraction;

import com.example.rms.service.event.implementation.IngredientStockAlertEvent;

public interface MerchantStockAlertEventHandler extends MerchantAlertEventHandler<IngredientStockAlertEvent> {
    void handle(IngredientStockAlertEvent event);
}
