package com.example.demo.dto;

import java.time.LocalDateTime;
import java.util.List;

public class StatisticsRequest {
    // 统计方式：BY_DEVICE(按设备), BY_PRODUCT(按产品), BY_BATCH(按批次), BY_TIME(按时间), BY_INDICATOR(按指标)
    private String statisticsType;
    
    // 统计内容：可多选 WEIGHT(重量), THICKNESS(厚度), HARDNESS(硬度), DIAMETER(直径), FRIABILITY(脆碎度)
    private List<String> statisticsContent;
    
    // 时间范围
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    
    // 设备筛选（可选）
    private List<String> deviceCodes;
    
    // 产品筛选（可选）
    private List<String> productIds;
    
    // 批次筛选（可选）
    private List<String> batchNos;
    
    // 聚合方式：AVG(平均值), SUM(总和), MAX(最大值), MIN(最小值), COUNT(数量)
    private String aggregationType;

    public String getStatisticsType() {
        return statisticsType;
    }

    public void setStatisticsType(String statisticsType) {
        this.statisticsType = statisticsType;
    }

    public List<String> getStatisticsContent() {
        return statisticsContent;
    }

    public void setStatisticsContent(List<String> statisticsContent) {
        this.statisticsContent = statisticsContent;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public List<String> getDeviceCodes() {
        return deviceCodes;
    }

    public void setDeviceCodes(List<String> deviceCodes) {
        this.deviceCodes = deviceCodes;
    }

    public List<String> getProductIds() {
        return productIds;
    }

    public void setProductIds(List<String> productIds) {
        this.productIds = productIds;
    }

    public List<String> getBatchNos() {
        return batchNos;
    }

    public void setBatchNos(List<String> batchNos) {
        this.batchNos = batchNos;
    }

    public String getAggregationType() {
        return aggregationType;
    }

    public void setAggregationType(String aggregationType) {
        this.aggregationType = aggregationType;
    }
}

