package com.example.rms.service.exception;

import com.example.rms.service.pattern.pipeline.Step;

public class StockUpdateFailedException extends Step.StepException {
    public StockUpdateFailedException() {
        super();
    }

    public StockUpdateFailedException(Throwable t) {
        super(t);
    }
}
