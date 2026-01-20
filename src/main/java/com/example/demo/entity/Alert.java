package com.example.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "alert")
public class Alert {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "device_id", referencedColumnName = "deviceid")
    private Device device;

    @Column(name = "alert_time")
    private LocalDateTime alertTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "alert_level", columnDefinition = "ENUM('YELLOW','RED')")
    private AlertLevel alertLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "alert_type", columnDefinition = "ENUM('DRUG','MECHINE')")
    private AlertType alertType;

    @Column(name = "context", columnDefinition = "LONGTEXT")
    private String context;

    // 枚举定义
    public enum AlertLevel {
        YELLOW, RED
    }

    public enum AlertType {
        DRUG, MECHINE
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    public LocalDateTime getAlertTime() {
        return alertTime;
    }

    public void setAlertTime(LocalDateTime alertTime) {
        this.alertTime = alertTime;
    }

    public AlertLevel getAlertLevel() {
        return alertLevel;
    }

    public void setAlertLevel(AlertLevel alertLevel) {
        this.alertLevel = alertLevel;
    }

    public AlertType getAlertType() {
        return alertType;
    }

    public void setAlertType(AlertType alertType) {
        this.alertType = alertType;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }
}