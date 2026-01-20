package com.example.demo.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import com.example.demo.entity.RolePermission;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RolePermissionRepository extends JpaRepository<RolePermission, Integer> {
    
    @Query("SELECT CASE WHEN COUNT(rp) > 0 THEN true ELSE false END " +
           "FROM RolePermission rp " +
           "WHERE rp.role.roleId = :roleId AND rp.permission.permission = :permName")
    boolean existsByRoleIdAndPermissionName(Integer roleId, String permName);

    // 查询角色的所有权限关联（用于展示角色当前拥有的权限）
    @Query("SELECT rp FROM RolePermission rp WHERE rp.role.roleId = :roleId")
    List<RolePermission> findByRoleId(Integer roleId);

    // 删除角色的所有权限（用于批量更新权限时先清空旧权限）
    @Modifying
    @Transactional
    @Query("DELETE FROM RolePermission rp WHERE rp.role.roleId = :roleId")
    void deleteByRoleId(Integer roleId);

    // 检查角色和权限的关联是否存在
    @Query("SELECT CASE WHEN COUNT(rp) > 0 THEN true ELSE false END " +
            "FROM RolePermission rp " +
            "WHERE rp.role.roleId = :roleId AND rp.permission.permissionId = :permissionId")
    boolean existsByRoleIdAndPermissionId(@Param("roleId") Integer roleId, @Param("permissionId") Integer permissionId);

    // 通过角色ID和权限ID删除关联
    @Modifying
    @Transactional
    @Query("DELETE FROM RolePermission rp " +
            "WHERE rp.role.roleId = :roleId " +  // 这里的roleId对应Role实体的主键字段
            "AND rp.permission.permissionId = :permissionId")  // permissionId对应Permission实体的主键字段
    void deleteByRoleIdAndPermissionId(@Param("roleId") Integer roleId, @Param("permissionId") Integer permissionId);

    // 通过权限ID删除所有相关关联（删除权限前先清理关联）
    @Modifying
    @Transactional
    @Query("DELETE FROM RolePermission rp WHERE rp.permission.permissionId = :permissionId")
    void deleteByPermissionId(@Param("permissionId") Integer permissionId);
}