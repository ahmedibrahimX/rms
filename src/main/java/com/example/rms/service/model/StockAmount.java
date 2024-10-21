package com.example.rms.service.model;

import java.math.BigDecimal;

public record StockAmount(Long ingredientId, BigDecimal amountInKilos) {
}
