package com.example.demo.service;

import com.example.demo.dto.StatisticsRequest;
import com.example.demo.entity.InspectionData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatisticsService {

    @Autowired
    private InspectDataService inspectDataService;

    // 执行统计分析
    public Map<String, Object> performStatistics(StatisticsRequest request) {
        // 获取数据
        List<InspectionData> dataList = inspectDataService.getInspectionsByFilters(
            request.getDeviceCodes(),
            request.getBatchNos(),
            request.getProductIds(),
            request.getStartTime() != null ? request.getStartTime().atZone(java.time.ZoneId.systemDefault()) : null,
            request.getEndTime() != null ? request.getEndTime().atZone(java.time.ZoneId.systemDefault()) : null
        );

        Map<String, Object> result = new HashMap<>();
        result.put("totalCount", dataList.size());
        result.put("statisticsType", request.getStatisticsType());
        result.put("aggregationType", request.getAggregationType());

        // 根据统计方式分组
        Map<String, List<InspectionData>> groupedData = groupData(dataList, request.getStatisticsType());
        
        // 计算统计数据
        List<Map<String, Object>> chartData = new ArrayList<>();
        for (Map.Entry<String, List<InspectionData>> entry : groupedData.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", entry.getKey());
            item.put("count", entry.getValue().size());
            
            // 计算各项指标
            for (String content : request.getStatisticsContent()) {
                List<BigDecimal> values = extractValues(entry.getValue(), content);
                BigDecimal aggregated = aggregate(values, request.getAggregationType());
                item.put(content.toLowerCase(), aggregated != null ? aggregated.doubleValue() : 0);
            }
            
            chartData.add(item);
        }

        result.put("chartData", chartData);
        result.put("rawData", dataList.stream().limit(100).collect(Collectors.toList())); // 限制原始数据量

        return result;
    }

    // 分组数据
    private Map<String, List<InspectionData>> groupData(List<InspectionData> dataList, String statisticsType) {
        Map<String, List<InspectionData>> grouped = new LinkedHashMap<>();
        
        switch (statisticsType) {
            case "BY_DEVICE":
                grouped = dataList.stream().collect(Collectors.groupingBy(
                    d -> d.getDevice() != null ? d.getDevice().getDeviceCode() : "未知设备",
                    LinkedHashMap::new,
                    Collectors.toList()
                ));
                break;
            case "BY_PRODUCT":
                grouped = dataList.stream().collect(Collectors.groupingBy(
                    d -> d.getProduct() != null ? d.getProduct().getProductName() : "未知产品",
                    LinkedHashMap::new,
                    Collectors.toList()
                ));
                break;
            case "BY_BATCH":
                grouped = dataList.stream().collect(Collectors.groupingBy(
                    InspectionData::getBatchNo,
                    LinkedHashMap::new,
                    Collectors.toList()
                ));
                break;
            case "BY_TIME":
                // 按日期分组
                grouped = dataList.stream().collect(Collectors.groupingBy(
                    d -> d.getInspectTime().toLocalDate().toString(),
                    LinkedHashMap::new,
                    Collectors.toList()
                ));
                break;
            default:
                // 默认按设备分组
                grouped = dataList.stream().collect(Collectors.groupingBy(
                    d -> d.getDevice() != null ? d.getDevice().getDeviceCode() : "未知设备",
                    LinkedHashMap::new,
                    Collectors.toList()
                ));
        }
        
        return grouped;
    }

    // 提取指标值
    private List<BigDecimal> extractValues(List<InspectionData> dataList, String content) {
        List<BigDecimal> values = new ArrayList<>();
        for (InspectionData data : dataList) {
            BigDecimal value = null;
            switch (content.toUpperCase()) {
                case "WEIGHT":
                    value = data.getWeight();
                    break;
                case "THICKNESS":
                    value = data.getThickness();
                    break;
                case "HARDNESS":
                    value = data.getHardness();
                    break;
                case "DIAMETER":
                    value = data.getDiameter();
                    break;
                case "FRIABILITY":
                    value = data.getFriability();
                    break;
            }
            if (value != null) {
                values.add(value);
            }
        }
        return values;
    }

    // 聚合计算
    private BigDecimal aggregate(List<BigDecimal> values, String aggregationType) {
        if (values.isEmpty()) {
            return null;
        }
        
        switch (aggregationType.toUpperCase()) {
            case "AVG":
                return values.stream()
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(values.size()), 2, BigDecimal.ROUND_HALF_UP);
            case "SUM":
                return values.stream()
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            case "MAX":
                return values.stream()
                    .max(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);
            case "MIN":
                return values.stream()
                    .min(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);
            case "COUNT":
                return BigDecimal.valueOf(values.size());
            default:
                return values.stream()
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(values.size()), 2, BigDecimal.ROUND_HALF_UP);
        }
    }
}

