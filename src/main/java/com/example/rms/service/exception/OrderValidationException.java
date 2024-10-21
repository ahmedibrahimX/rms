package com.example.rms.service.exception;

import com.example.rms.service.pattern.pipeline.Step;

public class OrderValidationException extends Step.StepException implements ValidationException {

    public OrderValidationException(String message) {
        super(message);
    }
}
