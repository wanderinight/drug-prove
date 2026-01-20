package com.example.demo.entity;

import lombok.Data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "operation_audit")
@Data
public class OperationAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 20)
    private String operationType; // CREATE/UPDATE/DELETE/LOGIN等
    
    @Column(nullable = false, length = 50)
    private String operationModule; // 设备/角色/用户等
    
    @Column(length = 500)
    private String operationContent;
    
    private Integer operatorId;
    
    @Column(length = 50)
    private String operatorName;
    
    @Column(nullable = false)
    private LocalDateTime operationTime;
    
    @Column(nullable = false, length = 20)
    private String operationStatus; // SUCCESS/FAILED
    
    @Column(length = 100)
    private String methodName;
    
    @Column(length = 100)
    private String className;
}
