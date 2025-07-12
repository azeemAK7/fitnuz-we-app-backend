package com.fitnuz.project.Security.Request;

import com.fitnuz.project.Model.Role;
import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignUpRequest {

    @NotBlank
    @Size(max = 20,message = "userName can be a maximum of 20 character")
    private String userName;

    @NotBlank
    @Size(max = 30, message = "Password can be a maximum of 30 characters")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).{8,30}$",
            message = "Password must be 8-30 characters long, include at least one uppercase letter, one lowercase letter, one digit, and one special character"
    )
    private String password;

    @NotBlank
    @Size(max = 30,message = "Email can be maximum of 30 character")
    @Email
    private String email;

    private Set<String> userRoles;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Set<String> getUserRoles() {
        return userRoles;
    }

    public void setUserRoles(Set<String> userRoles) {
        this.userRoles = userRoles;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
