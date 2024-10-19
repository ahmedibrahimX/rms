package com.example.rms.infra.repo;


import com.example.rms.infra.entity.ProductIngredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductIngredientRepo extends JpaRepository<ProductIngredient, UUID> {
    List<ProductIngredient> findAllByProductId(Long productId);
}