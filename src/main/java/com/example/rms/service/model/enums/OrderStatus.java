package com.example.rms.service.model.enums;

import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
public enum OrderStatus {
    PLACED("PLACED");

    String value;
    OrderStatus(String value) {
        this.value = value;
    }
}
