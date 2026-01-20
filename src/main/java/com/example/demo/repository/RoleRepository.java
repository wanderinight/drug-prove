package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.demo.entity.Role;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    // 检查角色名是否已存在（新增角色时防重复）
    boolean existsByRoleName(String roleName);
}