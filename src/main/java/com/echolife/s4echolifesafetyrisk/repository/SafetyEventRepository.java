package com.echolife.s4echolifesafetyrisk.repository;

import com.echolife.s4echolifesafetyrisk.entity.SafetyEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SafetyEventRepository extends JpaRepository<SafetyEvent, UUID> {
    List<SafetyEvent> findByUserIdOrderByCreatedAtDesc(String userId);
}