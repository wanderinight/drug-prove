package com.example.demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.Role;
import com.example.demo.entity.StaffRole;

public interface StaffRoleRepository extends JpaRepository<StaffRole, Integer> {

    // 查询用户的角色（返回单个角色）
    @Query("SELECT sr.role FROM StaffRole sr WHERE sr.staff.staffId = :staffId")
    Optional<Role> findRoleByStaffId(Integer staffId);

    // 根据员工ID查询关联记录
    @Query("SELECT sr FROM StaffRole sr WHERE sr.staff.staffId = :staffId")
    Optional<StaffRole> findByStaffId(@Param("staffId") Integer staffId);

    // 根据员工ID删除角色关联
    @Modifying
    @Transactional
    @Query("DELETE FROM StaffRole sr WHERE sr.staff.staffId = :staffId")
    //void deleteByStaffId(@Param("staffId") Integer staffId);
    void deleteByStaffId(Integer staffId);
}