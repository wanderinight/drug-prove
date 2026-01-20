package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.entity.StaffDeviceOperation;
import com.example.demo.service.StaffDeviceOperationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/operation")
public class StaffDeviceOperationController {

    @Autowired
    private StaffDeviceOperationService operationService;

    // 新增操作记录
    @PostMapping("/add")
    public ResponseEntity<Result> addOperation(@RequestBody StaffDeviceOperation operation) {
        try {
            StaffDeviceOperation saved = operationService.addOperation(operation);
            return ResponseEntity.ok(Result.success(saved, "操作记录添加成功"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Result.error("400", e.getMessage()));
        }
    }

    // 根据员工号查询操作记录
    @GetMapping("/by-staff")
    public ResponseEntity<Result> getByStaff(@RequestParam String staffcode) {
        List<StaffDeviceOperation> operations = operationService.getOperationsByStaff(staffcode);
        return ResponseEntity.ok(Result.success(operations));
    }

    // 根据设备号查询操作记录
    @GetMapping("/by-device")
    public ResponseEntity<Result> getByDevice(@RequestParam String deviceCode) {
        List<StaffDeviceOperation> operations = operationService.getOperationsByDevice(deviceCode);
        return ResponseEntity.ok(Result.success(operations));
    }

    // 根据员工号和设备号查询操作记录
    @GetMapping("/by-staff-device")
    public ResponseEntity<Result> getByStaffAndDevice(
            @RequestParam String staffcode,
            @RequestParam String deviceCode
    ) {
        List<StaffDeviceOperation> operations = operationService.getOperationsByStaffAndDevice(staffcode, deviceCode);
        return ResponseEntity.ok(Result.success(operations));
    }
}