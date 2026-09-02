package com.echolife.s4echolifesafetyrisk.repository;

import com.echolife.s4echolifesafetyrisk.entity.SafetyPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SafetyPolicyRepository extends JpaRepository<SafetyPolicy, UUID> {
    Optional<SafetyPolicy> findFirstByActiveTrueOrderByVersionDesc();
    Optional<SafetyPolicy> findFirstByTenantIdAndActiveTrueOrderByVersionDesc(String tenantId);
}