package com.example.rms.service.pattern.decorator;

import com.example.rms.service.pattern.pipeline.Step;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

@Slf4j
public class RetriableStepDecorator<I,O> implements Step<I, O> {
	private final int MAX_ATTEMPTS;
	private final int MULTIPLIER;
	private final long DELAY;
	private final Step<I, O> step;
	private final List<Type> RETRY_FOR;
	@Setter
    private Function<StepException, O> fallback = (exception) -> {
		log.error("Fallback method: retrials exhausted, keeps giving {}", exception.toString());
		throw exception;
	};

	public RetriableStepDecorator(Step<I, O> step, Integer maxAttempts, Long delay, Integer multiplier, Type... specificExceptionsOnly) {
		this.step = step;
		this.DELAY = delay;
		this.MAX_ATTEMPTS = maxAttempts;
		this.MULTIPLIER = multiplier;
		RETRY_FOR = specificExceptionsOnly.length == 0 ? List.of(Exception.class) : List.of(specificExceptionsOnly);
	}

	@Override
	public O process(I input) throws StepException {
		StepException exception = null;
		long delay = DELAY;
		for (int counter = 1; counter <= MAX_ATTEMPTS; counter++) {
			try {
				return step.process(input);
			} catch (Exception e) {
				if (RETRY_FOR.stream().noneMatch(clazz -> ((Class<?>)clazz).isInstance(e))) {
					log.info("Caught an exception that is not in the retriable exceptions. Retrial skipped.");
					throw e;
				}

				exception = e instanceof StepException ? (StepException) e : new StepException(e);
				log.error("failed attempt {} / {}", counter, MAX_ATTEMPTS);
				log.error(e.getMessage());
				log.error(Arrays.toString(e.getStackTrace()));

				try {
					Thread.sleep(delay);
					delay *= MULTIPLIER;
				} catch (Exception e1) {
					log.error(e1.getMessage());
					log.error(Arrays.toString(e1.getStackTrace()));
				}
			}
		}

		return fallback.apply(exception);
	}
}

