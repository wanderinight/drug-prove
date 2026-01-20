package com.example.demo.service;

import com.example.demo.entity.Device;
import com.example.demo.entity.Staff;
import com.example.demo.repository.DeviceRepository;
import com.example.demo.repository.StaffRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.demo.entity.StaffDevice;
import com.example.demo.repository.StaffDeviceRepository;
import java.util.List;

@Service
public class StaffDeviceService {

    @Autowired
    private StaffDeviceRepository staffDeviceRepository;


    @Autowired
    private StaffRepository staffRepository; // 假设存在Staff的Repository

    @Autowired
    private DeviceRepository deviceRepository; // 假设存在Device的Repository

    public StaffDevice save(StaffDevice staffDevice) {
        // 获取持久化的 Staff 对象
        Staff staff = staffRepository.findByStaffcode(staffDevice.getStaff().getStaffcode());
        if (staff == null) {
            throw new IllegalArgumentException("员工不存在: " + staffDevice.getStaff().getStaffcode());
        }

        // 获取持久化的 Device 对象（关键步骤）
        Device device = deviceRepository.findByDeviceCode(staffDevice.getDevice().getDeviceCode());
        if (device == null) {
            throw new IllegalArgumentException("设备不存在: " + staffDevice.getDevice().getDeviceCode());
        }

        // 设置持久化对象到关联中
        staffDevice.setStaff(staff);
        staffDevice.setDevice(device);

        return staffDeviceRepository.save(staffDevice);
    }

    // 移除员工对设备的关注
    public void delete(String staffcode, String deviceCode) {
        staffDeviceRepository.deleteByStaffCodeAndDeviceCode(staffcode, deviceCode);
    }

    // 获取某个员工关注的所有设备
    public List<StaffDevice> findByStaffCode(String staffcode) {
        return staffDeviceRepository.findByStaffCode(staffcode);
    }

    // 获取关注某个设备的所有员工
    public List<StaffDevice> findByDeviceCode(String deviceCode) {
        return staffDeviceRepository.findByDeviceCode(deviceCode);
    }
}