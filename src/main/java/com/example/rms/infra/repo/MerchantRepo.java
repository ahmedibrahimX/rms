package com.example.rms.infra.repo;


import com.example.rms.infra.entity.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface MerchantRepo extends JpaRepository<Merchant, UUID> {
}