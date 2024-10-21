package com.example.rms.service.exception;

import com.example.rms.service.pattern.pipeline.Step;

public class OrderValidationException extends ValidationException {

    public OrderValidationException() {
    }

    public OrderValidationException(String message) {
        super(message);
    }
}
