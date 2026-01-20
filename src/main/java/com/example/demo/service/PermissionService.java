package com.example.demo.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.entity.Role;
import com.example.demo.repository.RolePermissionRepository;
import com.example.demo.repository.StaffRoleRepository;

@Service
public class PermissionService {
    
    @Autowired
    private StaffRoleRepository staffRoleRepo;
    @Autowired
    private RolePermissionRepository rolePermRepo;

    /**
     * 检查员工是否有某权限
     * @param staffId 员工ID
     * @param permName 权限名（如："device:read"）
     */
    public boolean hasPermission(Integer staffId, String permName) {
        // 1. 获取员工所有角色
        Optional<Role> roles = staffRoleRepo.findRoleByStaffId(staffId);
        
        // 2. 检查这些角色是否拥有指定权限
        return roles.stream()
            .anyMatch(role -> 
                rolePermRepo.existsByRoleIdAndPermissionName(
                    role.getRoleId(), 
                    permName
                )
            );
    }
}
