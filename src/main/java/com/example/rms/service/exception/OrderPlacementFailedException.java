package com.example.rms.service.exception;

import com.example.rms.service.pattern.pipeline.Step;

public class OrderPlacementFailedException extends Step.StepException {
    public OrderPlacementFailedException() {
        super();
    }

    public OrderPlacementFailedException(Throwable t) {
        super(t);
    }
}
