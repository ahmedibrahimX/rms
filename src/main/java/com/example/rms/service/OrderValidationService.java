package com.example.rms.service;

import com.example.rms.infra.entity.Branch;
import com.example.rms.infra.repo.BranchRepo;
import com.example.rms.infra.repo.ProductRepo;
import com.example.rms.service.model.RequestedProductDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OrderValidationService {
    private final ProductRepo productRepo;
    private final BranchRepo branchRepo;
    private final Long maxTotalQuantity;
    private final Long maxProductQuantity;

    @Autowired
    public OrderValidationService(
            ProductRepo productRepo,
            BranchRepo branchRepo,
            @Value("${ordering.limit.max-total-quantity}") Long maxTotalQuantity,
            @Value("${ordering.limit.max-product-quantity}") Long maxProductQuantity
    ) {
        this.productRepo = productRepo;
        this.branchRepo = branchRepo;
        this.maxProductQuantity = maxProductQuantity;
        this.maxTotalQuantity = maxTotalQuantity;
    }

    public boolean validate(List<RequestedProductDetails> requestedProductDetails, UUID branchId) {
        if (requestedProductDetails.isEmpty()) return false;

        Optional<Branch> branch = branchRepo.findById(branchId);
        if (branch.isEmpty()) return false;

        long found = productRepo.countByMerchantIdAndIdIn(branch.get().merchantId(), requestedProductDetails.stream().map(RequestedProductDetails::productId).collect(Collectors.toSet()));
        if (found < requestedProductDetails.size()) return false;

        long totalCount = 0L;
        for (RequestedProductDetails details : requestedProductDetails) {
            if (details.quantity() > maxProductQuantity) return false;

            totalCount += details.quantity();
            if (totalCount > maxTotalQuantity) return false;
        }
        return true;
    }
}
