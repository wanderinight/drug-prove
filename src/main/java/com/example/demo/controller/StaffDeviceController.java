package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.demo.common.Result;
import com.example.demo.entity.StaffDevice;
import com.example.demo.service.StaffDeviceService;
import java.util.List;

@RestController
@RequestMapping("/api/staff-device")
public class StaffDeviceController {

    @Autowired
    private StaffDeviceService staffDeviceService;

    // 添加员工对设备的关注
    @PostMapping("/add")
    public ResponseEntity<Result> addStaffDevice(@RequestBody StaffDevice staffDevice) {
        StaffDevice savedStaffDevice = staffDeviceService.save(staffDevice);
        return ResponseEntity.ok(Result.success(savedStaffDevice));
    }

    // 移除员工对设备的关注
    @DeleteMapping("/delete") // 路径为/api/staff-device/delete
    public ResponseEntity<Result> delete(
            @RequestParam String staffcode,
            @RequestParam String deviceCode
    ) {
        System.out.println("进入删除接口: staffcode=" + staffcode + ", deviceCode=" + deviceCode); // 打印日志
        staffDeviceService.delete(staffcode, deviceCode);
        return ResponseEntity.ok(Result.success("删除成功"));
    }

    // 获取某个员工关注的所有设备
    @GetMapping("/get-by-staff")
    public ResponseEntity<Result> getByStaffCode(@RequestParam String staffcode) {
        List<StaffDevice> staffDevices = staffDeviceService.findByStaffCode(staffcode);
        return ResponseEntity.ok(Result.success(staffDevices));
    }

    // 获取关注某个设备的所有员工
    @GetMapping("/get-by-device")
    public ResponseEntity<Result> getByDeviceCode(@RequestParam String deviceCode) {
        List<StaffDevice> staffDevices = staffDeviceService.findByDeviceCode(deviceCode);
        return ResponseEntity.ok(Result.success(staffDevices));
    }
}