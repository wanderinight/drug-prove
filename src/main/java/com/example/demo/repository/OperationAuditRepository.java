package com.example.demo.repository;

import com.example.demo.entity.OperationAudit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OperationAuditRepository extends JpaRepository<OperationAudit, Long> {
}