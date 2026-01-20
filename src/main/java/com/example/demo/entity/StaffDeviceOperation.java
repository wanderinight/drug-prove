package com.example.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "staff_device_operation") // 数据库表名
public class StaffDeviceOperation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 自增主键
    @Column(name = "id")
    private Integer id;

    // 员工号（关联staffinfo表的staffcode）
    @Column(name = "staffcode", nullable = false, length = 50)
    private String staffcode;

    // 设备号（关联devices表的device_code）
    @Column(name = "device_code", nullable = false, length = 50)
    private String deviceCode;

    // 操作信息（如“启动设备”“停止检测”“校准设备”等）
    @Column(name = "operation_info", nullable = false, length = 200)
    private String operationInfo;

    // 操作时间（自动填充）
    @Column(name = "operation_time", nullable = false, updatable = false)
    private LocalDateTime operationTime;

    // 自动填充操作时间
    @PrePersist
    protected void onCreate() {
        operationTime = LocalDateTime.now();
    }

    // Getter和Setter
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getStaffcode() { return staffcode; }
    public void setStaffcode(String staffcode) { this.staffcode = staffcode; }

    public String getDeviceCode() { return deviceCode; }
    public void setDeviceCode(String deviceCode) { this.deviceCode = deviceCode; }

    public String getOperationInfo() { return operationInfo; }
    public void setOperationInfo(String operationInfo) { this.operationInfo = operationInfo; }

    public LocalDateTime getOperationTime() { return operationTime; }
}