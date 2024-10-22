package com.example.rms.controller;

import com.example.rms.common.auth.RequireUser;
import com.example.rms.controller.model.OrderRequest;
import com.example.rms.controller.model.PlacedOrderResponse;
import com.example.rms.controller.validation.UUIDPattern;
import com.example.rms.service.abstraction.OrderingPipeline;
import com.example.rms.service.implementation.OrderingPipelineService;
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

    @RequireUser
    @PostMapping("/{branchId}")
    public ResponseEntity<PlacedOrderResponse> order(@RequestBody @Valid OrderRequest request, @PathVariable("branchId") @Valid @UUIDPattern UUID branchId) {
        var orderDetails = map(branchId, getCustomerId(), request);
        var placedOrder = orderingPipeline.placeOrder(orderDetails);
        return ResponseEntity.status(HttpStatus.CREATED).body(map(placedOrder));
    }
}
