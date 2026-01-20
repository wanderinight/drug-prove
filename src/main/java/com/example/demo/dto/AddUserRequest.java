package com.example.demo.dto;

import com.example.demo.entity.Staff;


public class AddUserRequest {
    private Staff staff; // 员工基本信息（staffname、staffcode、password等）
    private Integer roleId; // 新增用户时要分配的角色ID

    // staff 的 getter 方法
    public Staff getStaff() {
        return staff;
    }

    public void setStaff(Staff staff) {
        this.staff = staff;
    }

    // roleId 的 getter 方法
    public Integer getRoleId() {
        return roleId;
    }

    public void setRoleId(Integer roleId) {
        this.roleId = roleId;
    }

}