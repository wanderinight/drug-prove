package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.entity.Permission;
import com.example.demo.entity.Staff;

public interface StaffRepository extends JpaRepository<Staff, Integer> {
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN TRUE ELSE FALSE END FROM Staff s WHERE s.staffcode = :staffcode")
    boolean existsByStaffcode(String staffcode);

    // 根据员工工号查询员工信息
    @Query("SELECT s FROM Staff s WHERE s.staffcode = :staffcode")
    Staff findByStaffcode(String staffcode);

    // 获取员工的权限信息
    @Query("SELECT p FROM Staff s " +
            "JOIN s.staffRole sr " +
            "JOIN sr.role r " +
            "JOIN r.rolePermissions rp " +
            "JOIN rp.permission p " +
            "WHERE s.staffcode = :staffcode")
    List<Permission> getPermissionsByStaffcode(String staffcode);

    // 根据ID查询员工（确保存在此方法）
    Optional<Staff> findByStaffId(Integer staffId);
}