package com.fitnuz.project.Service.Implementations;

import com.fitnuz.project.Exception.CustomException.DuplicateResourceFoundException;
import com.fitnuz.project.Model.AppRoles;
import com.fitnuz.project.Model.Role;
import com.fitnuz.project.Model.User;
import com.fitnuz.project.Repository.RoleRepository;
import com.fitnuz.project.Repository.UserRepository;
import com.fitnuz.project.Security.Jwt.JwtUtils;
import com.fitnuz.project.Security.Request.SignUpRequest;
import com.fitnuz.project.Security.Response.SignUpResponse;
import com.fitnuz.project.Service.Definations.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public SignUpResponse registerUser(SignUpRequest signUpRequest) {
        if(userRepository.existsByUserName(signUpRequest.getUserName())){
            throw  new DuplicateResourceFoundException("UserName already exists please try creating different username");
        }
        if(userRepository.existsByEmail(signUpRequest.getEmail())){
            throw  new DuplicateResourceFoundException("Email already exists please try creating different email");
        }
        User user = new User(signUpRequest.getUserName(),
                passwordEncoder.encode(signUpRequest.getPassword()),
                signUpRequest.getEmail());

        Set<String> strRoles = signUpRequest.getUserRoles();
        Set<Role> roles = new HashSet<>();
        if(strRoles == null){
            Role role = roleRepository.findByRoleName(AppRoles.ROLE_USER)
                    .orElseThrow(()-> new RuntimeException("Error : role is not found"));
            roles.add(role);
        }else{
            strRoles.forEach(role->{
                switch (role.toLowerCase()){
                    case "admin" :
                        Role adminRole = roleRepository.findByRoleName(AppRoles.ROLE_ADMIN)
                                .orElseThrow(()-> new RuntimeException("Error : role is not found"));
                        roles.add(adminRole);
                        break;
                    case "seller" :
                        Role sellerRole = roleRepository.findByRoleName(AppRoles.ROLE_SELLER)
                                .orElseThrow(()-> new RuntimeException("Error : role is not found"));
                        roles.add(sellerRole);
                        break;
                    default:
                        Role userRole = roleRepository.findByRoleName(AppRoles.ROLE_USER)
                                .orElseThrow(()-> new RuntimeException("Error : role is not found"));
                        roles.add(userRole);
                }
            });
        }
        user.setRoles(roles);
        userRepository.save(user);

        return new SignUpResponse("User Registered Successfully");
    }

    @Override
    public ResponseCookie signOutUser() {
        ResponseCookie cookie = jwtUtils.removeCookie();
        return cookie;
    }
}