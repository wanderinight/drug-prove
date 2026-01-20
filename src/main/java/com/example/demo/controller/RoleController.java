package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.demo.common.Result;
import com.example.demo.entity.Role;
import com.example.demo.entity.RolePermission;
import com.example.demo.service.RoleService;
import com.example.demo.service.RolePermissionService;
import java.util.List;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    @Autowired
    private RoleService roleService;

    @Autowired
    private RolePermissionService rolePermService;

    // -------------------------- 角色种类管理 --------------------------
    /**
     * 新增角色
     */
    @PostMapping("/add")
    public ResponseEntity<Result> addRole(@RequestBody Role role) {
        Role savedRole = roleService.addRole(role);
        return ResponseEntity.ok(Result.success(savedRole));
    }

    /**
     * 修改角色
     */
    @PutMapping("/update/{roleId}")
    public ResponseEntity<Result> updateRole(
            @PathVariable Integer roleId,
            @RequestBody Role updatedRole
    ) {
        Role role = roleService.updateRole(roleId, updatedRole);
        return ResponseEntity.ok(Result.success(role));
    }

    /**
     * 删除角色
     */
    @DeleteMapping("/delete/{roleId}")
    public ResponseEntity<Result> deleteRole(@PathVariable Integer roleId) {
        roleService.deleteRole(roleId);
        return ResponseEntity.ok(Result.success("角色删除成功"));
    }

    /**
     * 查询所有角色
     */
    @GetMapping("/list")
    public ResponseEntity<Result> getAllRoles() {
        List<Role> roles = roleService.getAllRoles();
        return ResponseEntity.ok(Result.success(roles));
    }

    // -------------------------- 角色权限管理 --------------------------
    /**
     * 查询某一角色详情
     */
    @GetMapping("/{roleId}/permissions")
    public ResponseEntity<Result> getPermissionsByRoleId(@PathVariable Integer roleId) {
        List<RolePermission> permissions = rolePermService.getPermissionsByRoleId(roleId);
        return ResponseEntity.ok(Result.success(permissions));
    }

    /**
     * 给角色添加单个权限
     */
    @PostMapping("/{roleId}/add-permission/{permId}")
    public ResponseEntity<Result> addPermissionToRole(
            @PathVariable Integer roleId,
            @PathVariable Integer permId
    ) {
        rolePermService.addPermissionToRole(roleId, permId);
        return ResponseEntity.ok(Result.success("权限添加成功"));
    }

    /**
     * 从角色移除单个权限
     */
    @DeleteMapping("/{roleId}/del-permission/{permId}")
    public ResponseEntity<Result> removePermissionFromRole(
            @PathVariable Integer roleId,
            @PathVariable Integer permId
    ) {
        rolePermService.removePermissionFromRole(roleId, permId);
        return ResponseEntity.ok(Result.success("权限移除成功"));
    }

    /**
     * 批量设置角色的权限（全量更新）
     */
    @PutMapping("/update/{roleId}/permissions")
    public ResponseEntity<Result> setPermissionsForRole(
            @PathVariable Integer roleId,
            @RequestBody List<Integer> permIds // 请求体为权限ID列表，如 [1,2,3]
    ) {
        rolePermService.setPermissionsForRole(roleId, permIds);
        return ResponseEntity.ok(Result.success("角色权限更新成功"));
    }
}