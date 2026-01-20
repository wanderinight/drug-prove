package com.example.demo.entity;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name ="staffinfo")
public class Staff {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "staffid")
    private Integer staffId;

    //一对一关联（一个用户对应一个角色关联记录）
    @JsonIgnore
    @OneToOne(mappedBy = "staff")
    private StaffRole staffRole;

    @Column(name = "staffname")
    private String staffname;

    @Column(name = "staffcode")
    private String staffcode;

    @Column(name = "password")
    private String password;

    // 员工部门，对应表字段 department
    @Column(name = "department")
    private String department;

    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public Integer getStaffId() {
        return staffId;
    }
    public void setStaffId(Integer staffId) {
        this.staffId = staffId;
    }
    public String getStaffname() {
        return staffname;
    }
    public void setStaffname(String staffname) {
        this.staffname = staffname;
    }
    public String getStaffcode() {
        return staffcode;
    }
    public void setStaffcode(String staffcode) {
        this.staffcode = staffcode;
    }
    public StaffRole getStaffRole() {
        return staffRole;
    }

    public void setStaffRole(StaffRole staffRole) {
        this.staffRole = staffRole;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }
}
