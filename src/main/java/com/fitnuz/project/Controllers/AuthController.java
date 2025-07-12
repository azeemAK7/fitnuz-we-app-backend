package com.fitnuz.project.Controllers;


import com.fitnuz.project.Exception.CustomException.GeneralAPIException;
import com.fitnuz.project.Exception.CustomException.ResourceNotFoundException;
import com.fitnuz.project.Security.Jwt.JwtUtils;
import com.fitnuz.project.Security.Request.LoginRequest;
import com.fitnuz.project.Security.Request.SignUpRequest;
import com.fitnuz.project.Security.Response.LoginResponse;
import com.fitnuz.project.Security.Response.SignUpResponse;
import com.fitnuz.project.Security.Services.UserDetailsimpl;
import com.fitnuz.project.Service.Definations.UserService;
import com.fitnuz.project.Service.Implementations.UserServiceImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    UserService userService;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        Authentication authentication;
        try {
            authentication = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUserName(), loginRequest.getPassword()));
        } catch (Exception exception) {
            Map<String, Object> map = new HashMap<>();
            map.put("message", "Bad credentials");
            map.put("status", false);
            return new ResponseEntity<Object>(map, HttpStatus.NOT_FOUND);
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetailsimpl userDetails = (UserDetailsimpl) authentication.getPrincipal();

        ResponseCookie cookie = jwtUtils.generateCookieFromUserName(userDetails);

        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .toList();

        LoginResponse response = new LoginResponse(userDetails.getId(),userDetails.getUsername(),userDetails.getEmail(),roles);
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE,cookie.toString()).body(response);
    }

    @PostMapping("/signUp")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpRequest SignUpRequest){
       SignUpResponse signUpResponse = userService.registerUser(SignUpRequest);
       return new ResponseEntity<>(signUpResponse,HttpStatus.CREATED);
    }

    @PostMapping("/signout")
    public ResponseEntity<?> signOutUser(){
        ResponseCookie signUpResponse = userService.signOutUser();
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE,signUpResponse.toString()).body(new SignUpResponse("You have been signed out successfully"));
    }

    @GetMapping("/userName")
    public String getUserName(Authentication authentication){
        if(authentication != null){
           return authentication.getName();
        }else{
            return "";
        }
    }

    @GetMapping("/user")
    public ResponseEntity<?> getUser(Authentication authentication){
        if(authentication != null){
            UserDetailsimpl userDetail = (UserDetailsimpl) authentication.getPrincipal();
            List<String> roles = userDetail.getAuthorities().stream()
                    .map(item -> item.getAuthority())
                    .toList();

            LoginResponse response = new LoginResponse(userDetail.getId(),userDetail.getUsername(),userDetail.getEmail(),roles);
            return new ResponseEntity<>(response,HttpStatus.OK);
        }else{
            throw new GeneralAPIException("user not found");
        }
    }

}
