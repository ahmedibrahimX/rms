package com.example.rms.controller.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record OrderRequestItem(
        @NotNull(message = "productId missing")
        @Min(value = 1L, message = "invalid productId value")
        @JsonProperty("productId")
        Long productId,

        @NotNull(message = "quantity missing")
        @Min(value = 1, message = "invalid quantity value")
        @JsonProperty("quantity")
        Integer quantity
) {
}
