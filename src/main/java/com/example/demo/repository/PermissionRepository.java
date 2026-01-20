package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.demo.entity.Permission;

public interface PermissionRepository extends JpaRepository<Permission, Integer> {
    // 检查权限ID是否存在（添加权限时校验）
    boolean existsByPermissionId(Integer permissionId);
}