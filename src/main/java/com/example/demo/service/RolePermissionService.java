package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.demo.entity.Role;
import com.example.demo.entity.RolePermission;
import com.example.demo.entity.Permission;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.RolePermissionRepository;
import com.example.demo.repository.PermissionRepository;
import java.util.List;

@Service
public class RolePermissionService {

    @Autowired
    private RolePermissionRepository rolePermRepo;

    @Autowired
    private RoleRepository roleRepo;

    @Autowired
    private PermissionRepository permRepo;

    /**
     * 给角色添加单个权限（检查角色和权限是否存在，避免重复添加）
     */
    public void addPermissionToRole(Integer roleId, Integer permId) {
        // 校验角色是否存在
        Role role = roleRepo.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("角色不存在：" + roleId));
        // 校验权限是否存在
        Permission permission = permRepo.findById(permId)
                .orElseThrow(() -> new IllegalArgumentException("权限不存在：" + permId));
        // 检查是否已存在该关联（改为通过角色ID和权限ID查询）
        if (rolePermRepo.existsByRoleIdAndPermissionId(roleId, permId)) {
            throw new IllegalArgumentException("角色已拥有该权限：角色ID=" + roleId + "，权限ID=" + permId);
        }
        // 保存关联关系（不再使用复合主键）
        RolePermission relation = new RolePermission();
        relation.setRole(role);
        relation.setPermission(permission);
        rolePermRepo.save(relation);
    }

    /**
     * 从角色移除单个权限
     */
    public void removePermissionFromRole(Integer roleId, Integer permId) {
        // 检查关联是否存在
        if (!rolePermRepo.existsByRoleIdAndPermissionId(roleId, permId)) {
            throw new IllegalArgumentException("角色未拥有该权限：角色ID=" + roleId + "，权限ID=" + permId);
        }
        // 直接通过角色ID和权限ID删除
        rolePermRepo.deleteByRoleIdAndPermissionId(roleId, permId);
    }

    /**
     * 批量设置角色的权限（先清空旧权限，再添加新权限）
     */
    @Transactional
    public void setPermissionsForRole(Integer roleId, List<Integer> permIds) {
        // 校验角色是否存在
        if (!roleRepo.existsById(roleId)) {
            throw new IllegalArgumentException("角色不存在：" + roleId);
        }
        // 校验所有权限是否存在
        for (Integer permId : permIds) {
            if (!permRepo.existsById(permId)) {
                throw new IllegalArgumentException("权限不存在：" + permId);
            }
        }
        // 先删除角色的所有旧权限
        rolePermRepo.deleteByRoleId(roleId);
        // 批量添加新权限
        Role role = roleRepo.findById(roleId).get(); // 获取持久化角色对象
        for (Integer permId : permIds) {
            Permission permission = permRepo.findById(permId).get();
            RolePermission relation = new RolePermission();
            relation.setRole(role);
            relation.setPermission(permission);
            rolePermRepo.save(relation);
        }
    }

    /**
     * 查询角色的所有权限
     */
    public List<RolePermission> getPermissionsByRoleId(Integer roleId) {
        if (!roleRepo.existsById(roleId)) {
            throw new IllegalArgumentException("角色不存在：" + roleId);
        }
        return rolePermRepo.findByRoleId(roleId);
    }
}