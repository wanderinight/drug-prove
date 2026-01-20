package com.example.demo.service;

import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.demo.entity.Staff;
import com.example.demo.entity.Permission;
import com.example.demo.entity.Role;
import com.example.demo.exception.CustomException;
import com.example.demo.repository.StaffRepository;

@Service
public class UserService implements UserDetailsService {
    @Autowired
    private  StaffRepository staffRepository;
    @Autowired
    private  PasswordEncoder passwordEncoder;
    
    public Staff register(Staff staff) {//账号注册
        if (staffRepository.existsByStaffcode(staff.getStaffcode())) {
            throw new RuntimeException("Employee ID already exists");
        }
        
        staff.setPassword(passwordEncoder.encode(staff.getPassword()));
        return staffRepository.save(staff);
    }


     public Staff login(String staffcode, String password) {
        Staff staff = staffRepository.findByStaffcode(staffcode);//还需补充账号不存在情况
        if (staff == null) {
            throw new CustomException("500", "账号不存在");
        }
        if (!passwordEncoder.matches(password, staff.getPassword())) {
            throw new CustomException("500", "账号密码不匹配");
        }
        return staff;
    }
     @Override
    public UserDetails loadUserByUsername(String staffcode) throws UsernameNotFoundException {
        Staff staff = staffRepository.findByStaffcode(staffcode);
        if (staff == null) {
            throw new UsernameNotFoundException("用户不存在: " + staffcode);
        }
        
        // 将Staff转换为UserDetails对象
        return User.builder()
                .username(staff.getStaffcode())
                .password(staff.getPassword())
                // .roles(staff.getRole()) // 假设Staff有getRole()方法返回角色名称-----待补充-部门
                .build();
    }


    public Staff getStaffByStaffcode(String staffcode) {
        return staffRepository.findByStaffcode(staffcode);
    }

    // 获取员工的权限信息
    public List<Permission> getPermByStaffcode(String staffcode) {
        List<Permission> permissions = staffRepository.getPermissionsByStaffcode(staffcode);
        return permissions;
    }

    // public Staff authenticate(Integer staffId, String password) {
    //     Staff staff = staffRepository.findBystaffId(staffId)
    //             .orElseThrow(() -> new RuntimeException("User not found"));
        
    //     if (!passwordEncoder.matches(password, staff.getPassword())) {
    //         throw new RuntimeException("Invalid password");
    //     }
        
    //     if (!staff.getIsActive()) {
    //         throw new RuntimeException("Account is inactive");
    //     }
        
    //     staff.setLastLoginTime(LocalDateTime.now());
    //     staffRepository.save(staff);
        
    //     return staff;
    // }


}
