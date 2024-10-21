package com.example.rms.service.exception;

import com.example.rms.service.pattern.pipeline.Step;

public class InsufficientIngredientsException extends Step.StepException {
    public InsufficientIngredientsException() {
        super();
    }
}
