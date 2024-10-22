package com.example.rms.common.exception.handler;

import com.example.rms.service.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@ControllerAdvice
public class GlobalAdvice {

    @ExceptionHandler(value = {InsufficientIngredientsException.class})
    public ResponseEntity<ErrorResponse> handleInsufficientIngredients(InsufficientIngredientsException ex) {
        log.error(ex.getMessage());
        log.error(Arrays.toString(ex.getStackTrace()));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse("InvalidRequest", "Ingredients not available."));
    }

    @ExceptionHandler(value = {OrderValidationException.class})
    public ResponseEntity<ErrorResponse> handleOrderValidationFailure(OrderValidationException ex) {
        log.error(ex.getMessage());
        log.error(Arrays.toString(ex.getStackTrace()));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse("InvalidRequest", ex.getMessage()));
    }

    @ExceptionHandler(value = {MethodArgumentNotValidException.class})
    public ResponseEntity<ErrorResponse> handleValidationError(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult().getFieldErrors().stream().map(DefaultMessageSourceResolvable::getDefaultMessage).collect(Collectors.toList());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse("InvalidRequest", "Request validation error.", errors));
    }

    @ExceptionHandler(value = {OrderPlacementFailedException.class})
    public ResponseEntity<ErrorResponse> handleOrderPlacementFailure(OrderPlacementFailedException ex) {
        log.error(ex.getMessage());
        log.error(Arrays.toString(ex.getStackTrace()));
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("InternalServerError", "Try again later."));
    }

    @ExceptionHandler(value = {StockUpdateFailedException.class})
    public ResponseEntity<ErrorResponse> handleStockUpdateRetrialsFailure(StockUpdateFailedException ex) {
        log.error(ex.getMessage());
        log.error(Arrays.toString(ex.getStackTrace()));
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("InternalServerError", "Try again later."));
    }

    @ExceptionHandler(value = {UnauthorizedAccess.class})
    public ResponseEntity<ErrorResponse> handleUnAuthorizedAccess(UnauthorizedAccess ex) {
        log.error(ex.getMessage());
        log.error(Arrays.toString(ex.getStackTrace()));
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse("Unauthorized", "Not allowed."));
    }

    @ExceptionHandler(value = {Exception.class})
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
        log.error(ex.getMessage());
        log.error(Arrays.toString(ex.getStackTrace()));
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("InternalServerError", "Try again later."));
    }
}
