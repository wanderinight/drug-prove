package com.example.demo.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "staffrole",
        uniqueConstraints = {@UniqueConstraint(columnNames = "staffid")}) // 确保一个用户只能有一个角色
public class StaffRole {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;  // 单独自增主键

    @OneToOne  // 改为一对一关联
    @JoinColumn(name = "staffid", unique = true)  // 唯一约束，确保一个用户只关联一个角色
    private Staff staff;

    @ManyToOne
    @JoinColumn(name = "roleid")
    private Role role;

    // getter和setter
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Staff getStaff() {
        return staff;
    }

    public void setStaff(Staff staff) {
        this.staff = staff;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}