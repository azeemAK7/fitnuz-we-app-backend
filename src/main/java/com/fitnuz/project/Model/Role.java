package com.fitnuz.project.Model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Data
@AllArgsConstructor
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private Integer roleId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role_name",length = 20)
    private AppRoles roleName;

    public Role() {

    }


    public Integer getRoleId() {
        return roleId;
    }

    public void setRoleId(Integer roleId) {
        this.roleId = roleId;
    }

    public AppRoles getRoleName() {
        return roleName;
    }

    public void setRoleName(AppRoles roleName) {
        this.roleName = roleName;
    }

    public Role(AppRoles roleName) {
        this.roleName = roleName;
    }
}
