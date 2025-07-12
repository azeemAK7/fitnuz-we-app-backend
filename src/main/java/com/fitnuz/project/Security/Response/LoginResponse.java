package com.fitnuz.project.Security.Response;

import java.util.List;

public class LoginResponse {

    private Long id;
    private String userName;
    private String email;
    private List<String> userRoles;

    public String getUserName() {
        return userName;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public List<String> getUserRoles() {
        return userRoles;
    }

    public void setUserRoles(List<String> userRoles) {
        this.userRoles = userRoles;
    }


    public LoginResponse(Long id, String userName,String email, List<String> userRoles) {
        this.userRoles = userRoles;
        this.userName = userName;
        this.email = email;
        this.id = id;
    }
}
