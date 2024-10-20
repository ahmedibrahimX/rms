package com.example.rms.service;

import com.example.rms.infra.entity.Branch;
import com.example.rms.infra.repo.BranchRepo;
import com.example.rms.infra.repo.ProductIngredientRepo;
import com.example.rms.infra.repo.ProductRepo;
import com.example.rms.service.exception.OrderValidationException;
import com.example.rms.service.model.OrderPreparationDetails;
import com.example.rms.service.model.OrderPreparationDetails;
import com.example.rms.service.model.RequestedOrderItemDetails;
import com.example.rms.service.model.interfaces.OrderBase;
import com.example.rms.service.pattern.pipeline.Step;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OrderValidationService implements Step<OrderBase, OrderBase> {
    private final ProductRepo productRepo;
    private final BranchRepo branchRepo;
    private final ProductIngredientRepo productIngredientRepo;
    private final Long maxTotalQuantity;
    private final Long maxProductQuantity;

    @Autowired
    public OrderValidationService(
            ProductRepo productRepo,
            BranchRepo branchRepo, ProductIngredientRepo productIngredientRepo,
            @Value("${ordering.limit.max-total-quantity}") Long maxTotalQuantity,
            @Value("${ordering.limit.max-product-quantity}") Long maxProductQuantity
    ) {
        this.productRepo = productRepo;
        this.branchRepo = branchRepo;
        this.productIngredientRepo = productIngredientRepo;
        this.maxProductQuantity = maxProductQuantity;
        this.maxTotalQuantity = maxTotalQuantity;
    }

    public OrderBase process(OrderBase customerOrder) {
        if (customerOrder.orderItems().isEmpty()) throw new OrderValidationException("Order has no products.");

        Optional<Branch> branch = branchRepo.findById(customerOrder.branchId());
        if (branch.isEmpty()) throw new OrderValidationException("Order requested from a branch that doesn't exist.");

        long found = productRepo.countByMerchantIdAndIdIn(branch.get().merchantId(), customerOrder.orderItems().stream()
                .map(RequestedOrderItemDetails::productId).collect(Collectors.toSet()));
        if (found < customerOrder.orderItems().size()) throw new OrderValidationException("Order requested from an invalid merchant.");

        long totalCount = 0L;
        for (RequestedOrderItemDetails item : customerOrder.orderItems()) {
            if (productIngredientRepo.countByProductId(item.productId()) == 0) throw new OrderValidationException("Product ingredients missing");

            if (item.quantity() > maxProductQuantity) throw new OrderValidationException("Quantity limit exceeded for a single product.");

            totalCount += item.quantity();
            if (totalCount > maxTotalQuantity) throw new OrderValidationException("Quantity limit exceeded for a single order.");
        }
        return new OrderPreparationDetails(customerOrder);
    }
}
