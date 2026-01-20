package com.example.demo.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.annotation.AuditLog;
import com.example.demo.common.Result;
import com.example.demo.entity.Device;
import com.example.demo.entity.Staff;
import com.example.demo.service.DeviceService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/device")
public class DeviceController {
    
    @Autowired
    private DeviceService deviceService;
    
    @GetMapping("/getall")
    public ResponseEntity<Result> getAllDevices() {
        List<Device> devices = deviceService.getAllDevices();
        return ResponseEntity.ok(Result.success(devices));
    }

    @GetMapping("/stats")
    public Map<String, Long> getDeviceStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("totalDevices", deviceService.getTotalDeviceCount());
        stats.put("D4Devices", deviceService.getD4DeviceCount());
        return stats;
    }

    @GetMapping("/follow")// 获取关注设备列表
   public ResponseEntity<Result> getDevicefollow(@RequestParam String staffcode) {
        List<Device> device= deviceService.getDeviceByStaffCode(staffcode);
        // 提取所有设备的 devicecode 并转换为字符串列表
    // List<String> deviceCodes = devices.stream()
    //                                   .map(Device::getDevicecode)
    //                                   .toList();  // Java 16+ 使用 toList()，低版本可用 Collectors.toList()
        return ResponseEntity.ok(Result.success(device));
    }
    
    @GetMapping("/countall")// 获取所有设备数量
     public ResponseEntity<Result> getcountall() {
        return ResponseEntity.ok(Result.success(deviceService.getCountAll()));
    }
    
    @GetMapping("/waitcalibration-countnow")// 获取待校准设备数量
     public ResponseEntity<Result> getWaitCalibrationCountNow() {
        return ResponseEntity.ok(Result.success(deviceService.getWaitCalibrationCountNow()));
    }

    @GetMapping("/monitoron-countnow")// 获取监控中设备数量
     public ResponseEntity<Result> getMonitorOnCountNow() {
        return ResponseEntity.ok(Result.success(deviceService.getMonitorOnCountNow()));
    }
    @GetMapping("/monitorinterrupt-countnow")// 获取监控中断设备数量
     public ResponseEntity<Result> getMonitorInterruptCountNow() {
        return ResponseEntity.ok(Result.success(deviceService.getMonitorInterruptCountNow()));
    }
    @GetMapping("/monitoroff-countnow")// 获取未在监控中设备数量
     public ResponseEntity<Result> getMonitorOffCountNow() {
        return ResponseEntity.ok(Result.success(deviceService.getMonitorOffCountNow()));
    }
    @GetMapping("/alerton-countnow")// 获取告警中设备数量
     public ResponseEntity<Result> getAlertOnCountNow() {
        return ResponseEntity.ok(Result.success(deviceService.getAlertOnCountNow()));
    }
    @GetMapping("/operationalon-countnow")// 获取运行中设备数量
     public ResponseEntity<Result> getOperationalOnCountNow() {
        return ResponseEntity.ok(Result.success(deviceService.getOperationalOnCountNow()));
    }
    @GetMapping("/operationaloffline-countnow")// 获取待机设备数量
    public ResponseEntity<Result> getOperationalOfflineCountNow() {
        return ResponseEntity.ok(Result.success(deviceService.getOperationalOfflineCountNow()));
    }

    // 获取实时设备统计信息（在监控设备数量、实时报警数量、实时工作设备数量、实时待机设备数量）
    @GetMapping("/realtime-stats")
    public ResponseEntity<Result> getRealtimeStats() {
        Map<String, Object> stats = new HashMap<>();
        // 在监控设备数量 = 监控中设备数量
        stats.put("monitoringDevices", deviceService.getMonitorOnCountNow());
        // 实时报警数量 = 报警中设备数量
        stats.put("alertingDevices", deviceService.getAlertOnCountNow());
        // 实时工作设备数量 = 运行中设备数量
        stats.put("workingDevices", deviceService.getOperationalOnCountNow());
        // 实时待机设备数量 = 离线设备数量
        stats.put("standbyDevices", deviceService.getOperationalOfflineCountNow());
        return ResponseEntity.ok(Result.success(stats));
    }
    //查询设备校准情况----有待讨论，到底要什么情况？？？



    @GetMapping("/get/{deviceCode}")
    public ResponseEntity<Result> getDeviceByCode(@PathVariable String deviceCode) {
        try {
            Device device = deviceService.getDeviceByCode(deviceCode);
            return ResponseEntity.ok(Result.success(device));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Result.error("400", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Result.error());
        }
    }

    @AuditLog(operationType = "CREATE", module = "设备管理", description = "创建新设备")
    @PostMapping("/add")
    public ResponseEntity<Result> addDevice(@RequestBody Device device) {
        try {
            Device savedDevice = deviceService.addDevice(device);
            // 直接使用 success(Object data, String msg) 方法
            return ResponseEntity.ok(Result.success(savedDevice, "设备添加成功"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Result.error("400", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Result.error());
        }
    }

    @DeleteMapping("/delete/{deviceCode}")
    public ResponseEntity<Result> deleteDevice(@PathVariable String deviceCode) {
        try {
            deviceService.deleteDevice(deviceCode);
            // 无需返回数据时，传 null 作为数据
            return ResponseEntity.ok(Result.success(null, "设备删除成功"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Result.error("400", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Result.error());
        }
    }


    // 更新设备信息
    @PatchMapping("/patch/{deviceCode}")
    public ResponseEntity<Result> patchDevice(
            @PathVariable String deviceCode,
            @RequestBody Map<String, Object> updates
    ) {
        try {
            Device updatedDevice = deviceService.patchDevice(deviceCode, updates);
            return ResponseEntity.ok(Result.success(updatedDevice, "设备更新成功"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Result.error("400", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Result.error("更新设备失败: " ,e.getMessage()));
        }
    }
}
