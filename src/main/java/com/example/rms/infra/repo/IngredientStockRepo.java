package com.example.rms.infra.repo;


import com.example.rms.infra.entity.IngredientStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface IngredientStockRepo extends JpaRepository<IngredientStock, UUID> {
    Set<IngredientStock> findByBranchIdAndIngredientIdIn(UUID branchId, Set<Long> ingredientIds);
}