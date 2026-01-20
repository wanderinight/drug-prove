package com.example.demo.dto;

import java.time.LocalDateTime;
import java.util.List;

public class StatisticsExportRequest {
    private StatisticsRequest statisticsRequest;
    private String exportFormat; // PDF, WORD, EXCEL
    private boolean saveToServer; // 是否保存到服务器

    public StatisticsRequest getStatisticsRequest() {
        return statisticsRequest;
    }

    public void setStatisticsRequest(StatisticsRequest statisticsRequest) {
        this.statisticsRequest = statisticsRequest;
    }

    public String getExportFormat() {
        return exportFormat;
    }

    public void setExportFormat(String exportFormat) {
        this.exportFormat = exportFormat;
    }

    public boolean isSaveToServer() {
        return saveToServer;
    }

    public void setSaveToServer(boolean saveToServer) {
        this.saveToServer = saveToServer;
    }
}

