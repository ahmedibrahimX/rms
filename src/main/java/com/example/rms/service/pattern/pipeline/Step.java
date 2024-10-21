package com.example.rms.service.pattern.pipeline;

import com.example.rms.service.exception.CustomException;

public interface Step<I, O> {
    public static class StepException extends CustomException {
        public StepException() {
        }

        public StepException(String message) {
            super(message);
        }

        public StepException(String message, Throwable cause) {
            super(message, cause);
        }

        public StepException(Throwable cause) {
            super(cause);
        }

        protected StepException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
            super(message, cause, enableSuppression, writableStackTrace);
        }
    }
    public O process(I input) throws StepException;
}