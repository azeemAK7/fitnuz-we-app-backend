package com.fitnuz.project.Service.Definations;

import com.fitnuz.project.Security.Request.SignUpRequest;
import com.fitnuz.project.Security.Response.SignUpResponse;
import org.springframework.http.ResponseCookie;

public interface UserService {
    SignUpResponse registerUser(SignUpRequest signUpRequest);

    ResponseCookie signOutUser();
}
