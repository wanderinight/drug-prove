package com.example.demo.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

import com.example.demo.entity.Role;
import com.example.demo.entity.Staff;
import com.example.demo.entity.StaffRole;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.StaffRepository;
import com.example.demo.repository.StaffRoleRepository;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class PermissionAdminService {

    @Autowired
    private StaffRoleRepository staffRoleRepo;

    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 为员工更新角色（替换现有角色，若已有角色则先删除）
     */
    @Transactional
    public void updateRoleForStaff(Integer staffId, Integer roleId) {
        // 1. 验证员工和角色存在
        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new IllegalArgumentException("员工不存在：" + staffId));
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("角色不存在：" + roleId));

        // 2. 强制删除该员工的所有旧角色关联（无论是否存在）
        staffRoleRepo.deleteByStaffId(staffId);

        // 3. 插入新的角色关联
        StaffRole relation = new StaffRole();
        relation.setStaff(staff);
        relation.setRole(role);
        staffRoleRepo.save(relation);
    }

    /**
     * 添加新用户（仅保存基本信息，角色通过updateRoleForStaff分配）
     */
    public Staff addUser(Staff staff) {
        // 检查员工编码是否已存在
        if (staffRepository.existsByStaffcode(staff.getStaffcode())) {
            throw new IllegalArgumentException("员工编码已存在：" + staff.getStaffcode());
        }
        // 对密码进行加密
        if (staff.getPassword() != null && !staff.getPassword().isEmpty()) {
            staff.setPassword(passwordEncoder.encode(staff.getPassword()));
        }
        return staffRepository.save(staff);
    }
    /**
     * 添加新用户并分配角色（原子操作）
     */
    @Transactional // 确保事务一致性
    public Staff addUserAndAssignRole(Staff staff, Integer roleId) {
        // 1. 保存用户
        Staff savedStaff = this.addUser(staff);
        // 2. 分配角色
        this.updateRoleForStaff(savedStaff.getStaffId(), roleId);
        return savedStaff;
    }

    /**
     * 删除用户
     */
    @Transactional
    public void deleteUser(Integer staffId) {
        // 先删除用户的角色关联
        staffRoleRepo.deleteByStaffId(staffId);
        // 再删除用户本身
        staffRepository.deleteById(staffId);
    }


    /**
     * 查询用户当前角色
     */
    public Role getUserRole(Integer staffId) {
        return staffRoleRepo.findRoleByStaffId(staffId)
                .orElseThrow(() -> new IllegalArgumentException("员工未分配角色：" + staffId));
    }

    /**
     * 查询所有员工
     */
    public List<Staff> getAllStaff() {
        return staffRepository.findAll();
    }

    /**
     * 更新员工信息
     */
    @Transactional
    public Staff updateStaff(Integer staffId, Staff updatedStaff, Integer roleId) {
        // 1. 验证员工存在
        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new IllegalArgumentException("员工不存在：" + staffId));

        // 2. 更新基本信息（如果提供了新值）
        if (updatedStaff.getStaffname() != null && !updatedStaff.getStaffname().isEmpty()) {
            staff.setStaffname(updatedStaff.getStaffname());
        }
        if (updatedStaff.getDepartment() != null) {
            staff.setDepartment(updatedStaff.getDepartment());
        }
        // 如果提供了新密码，则加密并更新
        if (updatedStaff.getPassword() != null && !updatedStaff.getPassword().isEmpty()) {
            staff.setPassword(passwordEncoder.encode(updatedStaff.getPassword()));
        }

        // 3. 如果提供了角色ID，则更新角色
        if (roleId != null) {
            this.updateRoleForStaff(staffId, roleId);
        }

        // 4. 保存更新后的员工信息
        return staffRepository.save(staff);
    }
}