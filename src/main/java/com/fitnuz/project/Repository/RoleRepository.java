package com.fitnuz.project.Repository;

import com.fitnuz.project.Model.AppRoles;
import com.fitnuz.project.Model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role,Long> {
    Optional<Role> findByRoleName(AppRoles appRoles);
}
