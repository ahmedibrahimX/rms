package com.example.rms.infra.repo;


import com.example.rms.infra.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;
import java.util.UUID;

@Repository
public interface ProductRepo extends JpaRepository<Product, Long> {
    long countByMerchantIdAndIdIn(UUID merchantId, Set<Long> productIds);
}