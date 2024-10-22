package com.example.rms.infra.repo;


import com.example.rms.infra.entity.Customer;
import com.example.rms.infra.entity.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface IngredientRepo extends JpaRepository<Ingredient, Long> {
}