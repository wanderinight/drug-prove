package com.example.demo.repository;

import com.example.demo.entity.StaffDeviceOperation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface StaffDeviceOperationRepository extends JpaRepository<StaffDeviceOperation, Integer> {

    // 根据员工号查询操作记录
    List<StaffDeviceOperation> findByStaffcodeOrderByOperationTimeDesc(String staffcode);

    // 根据设备号查询操作记录
    List<StaffDeviceOperation> findByDeviceCodeOrderByOperationTimeDesc(String deviceCode);

    // 根据员工号和设备号查询操作记录（按时间倒序）
    @Query("SELECT o FROM StaffDeviceOperation o WHERE o.staffcode = :staffcode AND o.deviceCode = :deviceCode ORDER BY o.operationTime DESC")
    List<StaffDeviceOperation> findByStaffcodeAndDeviceCode(String staffcode, String deviceCode);
}