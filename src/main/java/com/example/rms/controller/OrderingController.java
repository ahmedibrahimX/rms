package com.example.rms.controller;

import com.example.rms.common.auth.RequireUser;
import com.example.rms.common.exception.handler.ErrorResponse;
import com.example.rms.controller.model.OrderRequest;
import com.example.rms.controller.model.PlacedOrderResponse;
import com.example.rms.controller.validation.UUIDPattern;
import com.example.rms.service.abstraction.OrderingPipeline;
import com.example.rms.service.implementation.OrderingPipelineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static com.example.rms.common.util.ContextUtil.getCustomerId;
import static com.example.rms.controller.mapper.OrderDetailsMapper.map;

@RestController
@RequestMapping("/api/v1/me/orders")
public class OrderingController {
    private final OrderingPipeline orderingPipeline;
    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    public OrderingController(OrderingPipeline orderingPipeline, ApplicationEventPublisher eventPublisher) {
        this.orderingPipeline = orderingPipeline;
        this.eventPublisher = eventPublisher;
    }

    @Operation(summary = "Place a new order", description = "Adds a new order if the ingredients are available in stock.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successfully created a new order", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PlacedOrderResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class), examples = @ExampleObject(value = "{ \n\t\"error\": \"InvalidRequest\",\n \t\"message\": \"descriptive message\",\n\t\"details\": [\"extra details\"]\n}"))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class), examples = @ExampleObject(value = "{ \n\t\"error\": \"Unauthorized\",\n \t\"message\": \"descriptive message\"}"))),
            @ApiResponse(responseCode = "500", description = "API cannot place a new order now due to an internal error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class), examples = @ExampleObject(value = "{ \n\t\"error\": \"InternalServerError\",\n \t\"message\": \"descriptive message\"}")))
    })
    @RequireUser
    @PostMapping("/{branchId}")
    public ResponseEntity<PlacedOrderResponse> order(
             @RequestBody
             @Valid
             OrderRequest request,
             @PathVariable("branchId")
             @Parameter(description = "id of the branch that will serve the order", example = "448f40a8-1ad1-43c4-84de-36672710bb80")
             @Valid
             @UUIDPattern
             UUID branchId) {
        var orderDetails = map(branchId, getCustomerId(), request);
        var placedOrder = orderingPipeline.placeOrder(orderDetails);
        return ResponseEntity.status(HttpStatus.CREATED).body(map(placedOrder));
    }
}
