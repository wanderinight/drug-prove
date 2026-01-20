package com.example.demo.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.demo.entity.Alert;
import com.example.demo.entity.Device;
import com.example.demo.repository.AlertRepository;
import com.example.demo.repository.DeviceRepository;

@Service
public class AlertService {

    @Autowired
    private AlertRepository alertRepository;

    @Autowired
    private DeviceRepository deviceRepository;

     // 默认最大分页大小
    private static final int MAX_PAGE_SIZE = 100;
    public Object getTodayAlertCount(String currentTime) {//时间问题未解决
        // 可以将 currentTime 转换为 LocalDateTime 进行数据库查询
        // 示例：查找在此时间之前触发的告警数量
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneOffset.UTC);
            LocalDateTime parsedTime = LocalDateTime.parse(currentTime, formatter);
            return alertRepository.countToday(parsedTime);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("时间格式错误，请使用 yyyy-MM-dd HH:mm:ss");//待修改
        }
    }

    public Object getWeekAlertCount(String currentTime) {
        try {
            // 修正：使用当前时间作为基准，计算本周的开始和结束时间
            LocalDateTime now = LocalDateTime.now();
            if (currentTime != null && !currentTime.isEmpty()) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                try {
                    now = LocalDateTime.parse(currentTime, formatter);
                } catch (DateTimeParseException e) {
                    // 如果解析失败，使用当前时间
                    now = LocalDateTime.now();
                }
            }
            return alertRepository.countWeek(now);
        } catch (Exception e) {
            throw new IllegalArgumentException("获取本周报警数量失败: " + e.getMessage());
        }
    }

    // 获取本周报警情况汇总
    public Map<String, Object> getWeekAlertSummary(String currentTime) {
        try {
            LocalDateTime now = LocalDateTime.now();
            if (currentTime != null && !currentTime.isEmpty()) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                try {
                    now = LocalDateTime.parse(currentTime, formatter);
                } catch (DateTimeParseException e) {
                    now = LocalDateTime.now();
                }
            }
            
            Long yellowCount = alertRepository.countWeekYellow(now);
            Long redCount = alertRepository.countWeekRed(now);
            Long machineFailureCount = alertRepository.countWeekMachineFailure(now);
            
            return Map.of(
                "yellowAlert", yellowCount != null ? yellowCount : 0L,
                "redAlert", redCount != null ? redCount : 0L,
                "machineFailure", machineFailureCount != null ? machineFailureCount : 0L
            );
        } catch (Exception e) {
            throw new IllegalArgumentException("获取本周报警汇总失败: " + e.getMessage());
        }
    }

      // 获取报警记录详情(分页)
    public Map<String, Object> getAlertDetails(int page, int size) {
        // 限制每页最大数量
        int pageSize = Math.min(size, MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(page, pageSize);//page：当前页码
        
        Page<Object[]> results = alertRepository.findAlertDetails(pageable);
        
        List<Map<String, Object>> content = results.getContent().stream()
            .map(record -> Map.of(
                "alertTime", record[0],      // 报警时间
                "deviceCode", record[1],    // 设备编号
                "deviceName", record[2],   // 设备名称
                "deviceType", record[3],   // 设备类型
                "location", record[4],      // 设备位置
                "monitorStatus", record[5], // 监控状态
                "alertType", record[6],     // 报警类型(DRUG/MECHINE)
                "alertLevel", record[7],   // 报警等级(YELLOW/RED)
                "context", record[8]        // 报警内容
            ))
            .collect(Collectors.toList());
            
        return Map.of(
            "content", content,                         //当前页数据
            "totalElements", results.getTotalElements(),   // 总记录数
            "totalPages", results.getTotalPages(),       // 总页数
            "currentPage", results.getNumber(),          // 当前页码
            "pageSize", results.getSize()                 // 每页大小
        );
    }

    // 带时间范围的报警记录查询
    public Map<String, Object> getAlertDetailsByTimeRange(
            LocalDateTime startTime, 
            LocalDateTime endTime,
            String deviceCode,
            int page, 
            int size) {
        
          // 1. 参数校验和默认值设置
        if (startTime == null) {
            startTime = LocalDateTime.now().minusDays(7); // 默认查最近7天
        }
        if (endTime == null) {
            endTime = LocalDateTime.now();
        }

        // 限制最大时间范围为3个月
        if (startTime.isBefore(endTime.minusMonths(3))) {
            startTime = endTime.minusMonths(3);
        }      

        int pageSize = Math.min(size, MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(page, pageSize);
        
        Page<Object[]> results = alertRepository.findAlertDetailsByTimeRange(
            startTime, endTime, deviceCode, pageable);
        
        List<Map<String, Object>> content = results.getContent().stream()
            .map(record -> Map.of(
               "alertTime", record[0],      // 报警时间
                "deviceCode", record[1],    // 设备编号
                "deviceName", record[2],    // 设备名称
                "deviceType", record[3],    // 设备类型
                "location", record[4],      // 设备位置
                "monitorStatus", record[5], // 监控状态
                "alertType", record[6],     // 报警类型(DRUG/MECHINE)
                "alertLevel", record[7],   // 报警等级(YELLOW/RED)
                "context", record[8]        // 报警内容
            ))
            .collect(Collectors.toList());
            
        return Map.of(
            "content", content,
            "totalElements", results.getTotalElements(),
            "totalPages", results.getTotalPages(),
            "currentPage", results.getNumber(),
            "pageSize", results.getSize()
        );
    }

    public List<Map<String, Object>> getDeviceAlertOverview() {
        List<Device> devices = deviceRepository.findAll();
        return devices.stream()
            .map(device -> {
                Alert latestAlert = alertRepository.findTopByDeviceDeviceIdOrderByAlertTimeDesc(device.getDeviceId());
                Map<String, Object> info = new HashMap<>();
                info.put("deviceId", device.getDeviceId());
                info.put("deviceCode", device.getDeviceCode());
                info.put("deviceName", device.getDeviceName());
                info.put("deviceType", device.getDeviceType());
                info.put("location", device.getLocation());
                info.put("monitorStatus", device.getMonitorStatus());
                info.put("operationalStatus", device.getOperationalStatus());
                info.put("alertStatus", device.getAlertStatus());
                info.put("latestAlertTime", latestAlert != null ? latestAlert.getAlertTime() : null);
                info.put("latestAlertLevel", latestAlert != null ? latestAlert.getAlertLevel() : null);
                info.put("latestAlertContext", latestAlert != null ? latestAlert.getContext() : null);
                return info;
            })
            .collect(Collectors.toList());
    }
}
