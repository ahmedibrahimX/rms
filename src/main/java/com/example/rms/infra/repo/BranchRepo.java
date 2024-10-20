package com.example.rms.infra.repo;


import com.example.rms.infra.entity.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface BranchRepo extends JpaRepository<Branch, UUID> {
}