package com.example.demo.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

@Entity
@Table(name = "devices")
public class Device {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "deviceid", nullable = false)
    private Integer deviceId;

    @Column(name = "device_code",length = 50, unique = true)
    private String deviceCode;

    @Column(name = "device_name",length = 50)
    private String deviceName;
    
    @Column(name = "location", length = 100)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(name = "device_type", columnDefinition = "ENUM('D4','DB')")
    private DeviceType deviceType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "monitor_status", nullable = false, columnDefinition = "ENUM('MONITORON','MONITORINTERRUPT','MONITOROFF')")
    private MonitorStatus monitorStatus = MonitorStatus.MONITOROFF;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "operational_status", nullable = false, columnDefinition = "ENUM('RUNNING','OFFLINE')")
    private OperationalStatus operationalStatus = OperationalStatus.OFFLINE;
   
    @Enumerated(EnumType.STRING)
    @Column(name = "alert_status", nullable = false, columnDefinition = "ENUM('ON','OFF')")
    private AlertStatus alertStatus = AlertStatus.OFF;

    @Enumerated(EnumType.STRING)
    @Column(name = "calibration_status", nullable = false, columnDefinition = "ENUM('NORMAL','PENDING')")
    private CalibrationStatus calibrationStatus = CalibrationStatus.PENDING;



    @Column(name = "self_calibration")
    private LocalDateTime selfCalibration;
    
    @Column(name = "last_calibration")
    private LocalDateTime lastCalibration;
    
    @Column(name = "next_calibration")
    private LocalDateTime nextCalibration;
    
    @Column(name = "calibration_operator", length = 20)
    private String calibrationOperator;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


        // createdAt的特殊处理（通常在@PrePersist中设置）
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    @PreUpdate
    protected void onUpdate() {
    updatedAt = LocalDateTime.now();
    }
   

    // 枚举定义
    public enum DeviceType {
        D4, DB
    }

    public enum OperationalStatus {
        RUNNING, OFFLINE
    }
    public enum MonitorStatus {
        MONITORON, MONITORINTERRUPT, MONITOROFF
    }
    public enum AlertStatus {
        ON, OFF
    }
    public enum CalibrationStatus {
        NORMAL, PENDING
    }


    // Getters and Setters
   public String getDeviceCode() {
        return deviceCode;
    }
    public void setDeviceCode(String deviceCode) {
        this.deviceCode = deviceCode;
    }
    public String getDeviceName() {
        return deviceName;
    }
    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }
    public String getLocation() {
        return location;
    }
    public void setLocation(String location) {
        this.location = location;
    }
    public MonitorStatus getMonitorStatus() {
        return monitorStatus;
    }
    public void setMonitorStatus(MonitorStatus monitorStatus) {
        this.monitorStatus = monitorStatus;
    }
    public OperationalStatus getOperationalStatus() {
        return operationalStatus;
    }
    public void setOperationalStatus(OperationalStatus operationalStatus) {
        this.operationalStatus = operationalStatus;
    }
    public LocalDateTime getSelfCalibration() {
        return selfCalibration;
    }
    public void setSelfCalibration(LocalDateTime selfCalibration) {
        this.selfCalibration = selfCalibration;
    }
    public LocalDateTime getLastCalibration() {
        return lastCalibration;
    }
    public void setLastCalibration(LocalDateTime lastCalibration) {
        this.lastCalibration = lastCalibration;
    }
    public LocalDateTime getNextCalibration() {
        return nextCalibration;
    }
    public void setNextCalibration(LocalDateTime nextCalibration) {
        this.nextCalibration = nextCalibration;
    }
    public String getCalibrationOperator() {
        return calibrationOperator;
    }
    public void setCalibrationOperator(String calibrationOperator) {
        this.calibrationOperator = calibrationOperator;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Integer getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Integer deviceId) {
        this.deviceId = deviceId;
    }

    public DeviceType getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(DeviceType deviceType) {
        this.deviceType = deviceType;
    }
    public AlertStatus getAlertStatus() {
        return alertStatus;
    }
    public void setAlertStatus(AlertStatus alertStatus) {
        this.alertStatus = alertStatus;
    }
    public CalibrationStatus getCalibrationStatus() {
        return calibrationStatus;
    }
    public void setCalibrationStatus(CalibrationStatus calibrationStatus) {
        this.calibrationStatus = calibrationStatus;
    }
}