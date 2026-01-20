package com.example.demo.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "roleprm")
public class RolePermission {
    @Id
    @Column(name = "permid")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer permid;  // 主键

    @ManyToOne
    @JoinColumn(name = "roleid")
    private Role role;

    @ManyToOne
    @JoinColumn(name = "permissionid")
    private Permission permission;

    public Integer getId() {
        return permid;
    }

    public void setId(Integer id) {
        this.permid = id;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Permission getPermission() {
        return permission;
    }

    public void setPermission(Permission permission) {
        this.permission = permission;
    }
}
