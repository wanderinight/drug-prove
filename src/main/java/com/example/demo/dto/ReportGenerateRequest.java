package com.example.demo.dto;

import java.util.List;

public class ReportGenerateRequest {
    private List<String> deviceCodes;
    private List<String> batchNos;
    private List<String> productIds;
    private String startTime;
    private String endTime;
    private Boolean saveFile;
    private String reportTitle;

    public List<String> getDeviceCodes() {
        return deviceCodes;
    }

    public void setDeviceCodes(List<String> deviceCodes) {
        this.deviceCodes = deviceCodes;
    }

    public List<String> getBatchNos() {
        return batchNos;
    }

    public void setBatchNos(List<String> batchNos) {
        this.batchNos = batchNos;
    }

    public List<String> getProductIds() {
        return productIds;
    }

    public void setProductIds(List<String> productIds) {
        this.productIds = productIds;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public Boolean getSaveFile() {
        return saveFile;
    }

    public void setSaveFile(Boolean saveFile) {
        this.saveFile = saveFile;
    }

    public String getReportTitle() {
        return reportTitle;
    }

    public void setReportTitle(String reportTitle) {
        this.reportTitle = reportTitle;
    }
}

