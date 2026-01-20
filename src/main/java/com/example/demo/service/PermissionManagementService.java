package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.Permission;
import com.example.demo.repository.PermissionRepository;
import com.example.demo.repository.RolePermissionRepository;

import java.util.List;

@Service
public class PermissionManagementService {

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private RolePermissionRepository rolePermissionRepository;

    /**
     * 查询所有权限
     */
    public List<Permission> getAllPermissions() {
        return permissionRepository.findAll();
    }

    /**
     * 添加权限
     */
    public Permission addPermission(Permission permission) {
        // 检查权限编码是否已存在（如果permissionCode不为空）
        if (permission.getPermissionCode() != null && !permission.getPermissionCode().isEmpty()) {
            // 可以根据需要添加唯一性检查
        }
        return permissionRepository.save(permission);
    }

    /**
     * 删除权限
     */
    @Transactional
    public void deletePermission(Integer permissionId) {
        // 检查权限是否存在
        if (!permissionRepository.existsById(permissionId)) {
            throw new IllegalArgumentException("权限不存在：" + permissionId);
        }
        // 先删除相关的角色权限关联
        rolePermissionRepository.deleteByPermissionId(permissionId);
        // 再删除权限本身
        permissionRepository.deleteById(permissionId);
    }
}

