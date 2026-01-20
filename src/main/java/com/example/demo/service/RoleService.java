package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.demo.entity.Role;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.RolePermissionRepository;
import java.util.List;
import java.util.Optional;

@Service
public class RoleService {

    @Autowired
    private RoleRepository roleRepo;

    @Autowired
    private RolePermissionRepository rolePermRepo; // 用于删除角色时级联删除权限关联

    /**
     * 新增角色（检查角色名是否重复）
     */
    public Role addRole(Role role) {
        if (roleRepo.existsByRoleName(role.getRoleName())) {
            throw new IllegalArgumentException("角色名已存在：" + role.getRoleName());
        }
        return roleRepo.save(role);
    }

    /**
     * 修改角色（允许修改名称和描述，需检查新名称是否与其他角色重复）
     */
    public Role updateRole(Integer roleId, Role updatedRole) {
        Role existingRole = roleRepo.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("角色不存在：" + roleId));

        // 若修改了角色名，需检查是否重复（排除自身）
        if (!existingRole.getRoleName().equals(updatedRole.getRoleName())
                && roleRepo.existsByRoleName(updatedRole.getRoleName())) {
            throw new IllegalArgumentException("角色名已存在：" + updatedRole.getRoleName());
        }

        existingRole.setRoleName(updatedRole.getRoleName());
        existingRole.setRoleDescription(updatedRole.getRoleDescription());
        return roleRepo.save(existingRole);
    }

    /**
     * 删除角色（级联删除角色的所有权限关联，避免外键约束错误）
     */
    @Transactional
    public void deleteRole(Integer roleId) {
        if (!roleRepo.existsById(roleId)) {
            throw new IllegalArgumentException("角色不存在：" + roleId);
        }
        // 先删除角色的所有权限关联
        rolePermRepo.deleteByRoleId(roleId);
        // 再删除角色本身
        roleRepo.deleteById(roleId);
    }

    /**
     * 查询所有角色
     */
    public List<Role> getAllRoles() {
        return roleRepo.findAll();
    }

    /**
     * 查询单个角色详情
     */
    public Role getRoleById(Integer roleId) {
        return roleRepo.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("角色不存在：" + roleId));
    }
}