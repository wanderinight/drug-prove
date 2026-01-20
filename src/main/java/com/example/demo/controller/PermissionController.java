package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.common.Result;
import com.example.demo.entity.Permission;
import com.example.demo.service.PermissionManagementService;

import java.util.List;

@RestController
@RequestMapping("/api/permissions")
public class PermissionController {

    @Autowired
    private PermissionManagementService permissionManagementService;

    /**
     * 查询所有权限
     */
    @GetMapping("/list")
    public ResponseEntity<Result> getAllPermissions() {
        List<Permission> permissions = permissionManagementService.getAllPermissions();
        return ResponseEntity.ok(Result.success(permissions));
    }

    /**
     * 添加权限
     */
    @PostMapping("/add")
    public ResponseEntity<Result> addPermission(@RequestBody Permission permission) {
        try {
            Permission savedPermission = permissionManagementService.addPermission(permission);
            return ResponseEntity.ok(Result.success(savedPermission, "权限添加成功"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Result.error("400", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Result.error());
        }
    }

    /**
     * 删除权限
     */
    @DeleteMapping("/delete/{permissionId}")
    public ResponseEntity<Result> deletePermission(@PathVariable Integer permissionId) {
        try {
            permissionManagementService.deletePermission(permissionId);
            return ResponseEntity.ok(Result.success(null, "权限删除成功"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Result.error("400", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Result.error());
        }
    }
}

