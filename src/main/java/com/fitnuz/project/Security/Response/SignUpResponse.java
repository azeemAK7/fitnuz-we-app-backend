package com.fitnuz.project.Security.Response;

import lombok.Data;

@Data
public class SignUpResponse {
    private String message;

    public SignUpResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
