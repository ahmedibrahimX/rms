package com.example.rms.controller.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record OrderRequest(
        @NotEmpty(message = "order must contain products")
        @Valid
        @JsonProperty("products")
        List<OrderRequestItem> products
) {
}
