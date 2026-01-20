package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Device;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Integer> {

    // 查询设备总数
    @Query("SELECT COUNT(d) FROM Device d")
    Long countTotalDevices();
    
    // 查询D4设备数量D
    @Query("SELECT COUNT(d) FROM Device d WHERE d.deviceType = 'D4'")
    Long counttype();

    // 查询staffcode关注的设备列表
    @Query("SELECT sd.device FROM StaffDevice sd WHERE sd.staff.staffcode = :staffcode")
    List<Device> findDevicesByStaffcode(String staffcode);

    // 新增方法：通过 deviceCode 查询单个设备
    @Query("SELECT d FROM Device d WHERE d.deviceCode = :deviceCode")
    Device findByDeviceCode(String deviceCode);

    // 查询所有设备数量
    @Query("SELECT COUNT(d) FROM Device d")
    Integer countAll();

    // 查询待校准设备数量
    @Query("SELECT COUNT(d) FROM Device d WHERE d.calibrationStatus = 'PENDING'")
    Integer countWaitCalibrationNow();
    // 查询监控中设备数量
    @Query("SELECT COUNT(d) FROM Device d WHERE d.monitorStatus = 'MONITORON'")
    Integer countMonitorOnNow();
    // 查询监控中断设备数量
    @Query("SELECT COUNT(d) FROM Device d WHERE d.monitorStatus = 'MONITORINTERRUPT'")
     Integer countMonitorInterruptNow();

    // 查询未在监控中设备数量
    @Query("SELECT COUNT(d) FROM Device d WHERE d.monitorStatus = 'MONITOROFF'")
    Integer countMonitorOffNow();

    // 查询警报中设备数量
    @Query("SELECT COUNT(d) FROM Device d WHERE d.alertStatus = 'ON'")
    Integer countAlertOnNow();

    // 查询运行中设备数量
    @Query("SELECT COUNT(d) FROM Device d WHERE d.operationalStatus = 'RUNNING'")
    Integer countOperationalOnNow();

    // 查询待机中设备数量
    @Query("SELECT COUNT(d) FROM Device d WHERE d.operationalStatus = 'OFFLINE'")
    Integer countOperationalOfflineNow();

    @Query("SELECT COUNT(d) > 0 FROM Device d WHERE d.deviceCode = :deviceCode")
    boolean existsByDeviceCode(String deviceCode);

    @Transactional
    @Modifying
    @Query("DELETE FROM Device d WHERE d.deviceCode = :deviceCode")
    void deleteByDeviceCode(String deviceCode);

}
