package com.example.demo.service;

import com.example.demo.entity.StaffDeviceOperation;
import com.example.demo.repository.StaffDeviceOperationRepository;
import com.example.demo.repository.StaffRepository;
import com.example.demo.repository.DeviceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class StaffDeviceOperationService {

    @Autowired
    private StaffDeviceOperationRepository operationRepository;

    @Autowired
    private StaffRepository staffRepository; // 用于校验员工是否存在

    @Autowired
    private DeviceRepository deviceRepository; // 用于校验设备是否存在

    // 新增操作记录（带参数校验）
    public StaffDeviceOperation addOperation(StaffDeviceOperation operation) {
        // 校验员工是否存在
        if (staffRepository.findByStaffcode(operation.getStaffcode()) == null) {
            throw new IllegalArgumentException("员工不存在：" + operation.getStaffcode());
        }
        // 校验设备是否存在
        if (deviceRepository.findByDeviceCode(operation.getDeviceCode()) == null) {
            throw new IllegalArgumentException("设备不存在：" + operation.getDeviceCode());
        }
        return operationRepository.save(operation);
    }

    // 根据员工号查询操作记录
    public List<StaffDeviceOperation> getOperationsByStaff(String staffcode) {
        return operationRepository.findByStaffcodeOrderByOperationTimeDesc(staffcode);
    }

    // 根据设备号查询操作记录
    public List<StaffDeviceOperation> getOperationsByDevice(String deviceCode) {
        return operationRepository.findByDeviceCodeOrderByOperationTimeDesc(deviceCode);
    }

    // 根据员工号和设备号查询操作记录
    public List<StaffDeviceOperation> getOperationsByStaffAndDevice(String staffcode, String deviceCode) {
        return operationRepository.findByStaffcodeAndDeviceCode(staffcode, deviceCode);
    }
}