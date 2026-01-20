package com.example.demo.repository;

import com.example.demo.entity.InspectionData;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;

@Repository
public interface InspectDataRepository extends JpaRepository<InspectionData, Integer>, JpaSpecificationExecutor<InspectionData> {
    
    // 获取所有设备的最新检测数据
    @EntityGraph(attributePaths = {"device", "product"})
    @Query("SELECT i FROM InspectionData i " +
           "WHERE i.inspectTime = (SELECT MAX(i2.inspectTime) FROM InspectionData i2 " +
           "                      WHERE i2.device.deviceId = i.device.deviceId " +
           "                      AND i2.isValid = 1)")
    List<InspectionData> findLatestInspections();
    
    // 获取单个设备的最新检测数据
    @EntityGraph(attributePaths = {"device", "product"})
    @Query("SELECT i FROM InspectionData i " +
           "WHERE i.device.deviceCode = :deviceCode AND i.isValid = 1 " +
           "ORDER BY i.inspectTime DESC")
    List<InspectionData> findLatestByDeviceId(String deviceCode);

    // 新增方法：按批次查询
    @EntityGraph(attributePaths = {"device", "product"})
    List<InspectionData> findByBatchNo(String batchNo);
    
    // 新增方法：按产品ID查询
    @EntityGraph(attributePaths = {"device", "product"})
    @Query("SELECT i FROM InspectionData i WHERE i.product.productId = :productId")
    List<InspectionData> findByProductId(String productId);
    
    // 新增方法：按时间范围查询
    @EntityGraph(attributePaths = {"device", "product"})
    List<InspectionData> findByInspectTimeBetween(ZonedDateTime start, ZonedDateTime end);
}
