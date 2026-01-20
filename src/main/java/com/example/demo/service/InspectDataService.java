package com.example.demo.service;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.example.demo.entity.InspectionData;
import com.example.demo.repository.InspectDataRepository;

import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;

@Service
public class InspectDataService {
    
    @Autowired
    private InspectDataRepository inspectDataRepository;
     // 获取所有设备的最新检测数据（包含设备信息）
    public List<InspectionData> getLatestInspectionsWithDeviceInfo() {
        return inspectDataRepository.findLatestInspections();
    }
    
    // 获取单个设备的最新检测数据
    public Optional<InspectionData> getLatestInspectionByDeviceId(String deviceCode) {
        List<InspectionData> result = inspectDataRepository.findLatestByDeviceId(deviceCode);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }
     // 新增方法：按批次获取检测数据
    public List<InspectionData> getInspectionsByBatch(String batchNo) {
        return inspectDataRepository.findByBatchNo(batchNo);
    }
    
    // 新增方法：按产品获取检测数据
    public List<InspectionData> getInspectionsByProduct(String productId) {
        return inspectDataRepository.findByProductId(productId);
    }
    
    // 新增方法：按时间范围获取检测数据
    public List<InspectionData> getInspectionsByTimeRange(ZonedDateTime start, ZonedDateTime end) {
        return inspectDataRepository.findByInspectTimeBetween(start, end);
    }

    public List<InspectionData> getInspectionsByFilters(
            List<String> deviceCodes,
            List<String> batchNos,
            List<String> productIds,
            ZonedDateTime start,
            ZonedDateTime end) {

        Specification<InspectionData> specification = (root, query, cb) -> {
            root.fetch("device", JoinType.LEFT);
            root.fetch("product", JoinType.LEFT);
            query.distinct(true);

            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("isValid"), 1));

            if (start != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("inspectTime"), start));
            }
            if (end != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("inspectTime"), end));
            }
            if (deviceCodes != null && !deviceCodes.isEmpty()) {
                predicates.add(root.get("device").get("deviceCode").in(deviceCodes));
            }
            if (batchNos != null && !batchNos.isEmpty()) {
                predicates.add(root.get("batchNo").in(batchNos));
            }
            if (productIds != null && !productIds.isEmpty()) {
                predicates.add(root.get("product").get("productId").in(productIds));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return inspectDataRepository.findAll(specification);
    }

}
