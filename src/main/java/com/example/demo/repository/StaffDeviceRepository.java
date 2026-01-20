package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.example.demo.entity.StaffDevice;
import java.util.List;
import org.springframework.data.jpa.repository.Modifying; // 导入注解
import org.springframework.transaction.annotation.Transactional; // 导入事务注解

public interface StaffDeviceRepository extends JpaRepository<StaffDevice, Integer> {

    // 根据员工编号查询关注的设备
    @Query("SELECT sd FROM StaffDevice sd WHERE sd.staff.staffcode = :staffcode")
    List<StaffDevice> findByStaffCode(String staffcode);

    // 根据设备编号查询关注的员工
    @Query("SELECT sd FROM StaffDevice sd WHERE sd.device.deviceCode = :deviceCode")
    List<StaffDevice> findByDeviceCode(String deviceCode);

    // 根据员工和设备编号删除关注关系
    @Modifying // 关键：标识这是修改操作（DELETE/UPDATE）
    @Transactional // 关键：删除操作需要事务支持
    @Query("DELETE FROM StaffDevice sd WHERE sd.staff.staffcode = :staffcode AND sd.device.deviceCode = :deviceCode")
    void deleteByStaffCodeAndDeviceCode(String staffcode, String deviceCode);
}