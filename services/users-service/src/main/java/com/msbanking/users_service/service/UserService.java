package com.msbanking.users_service.service;

import com.msbanking.users_service.dto.request.CreateUserRequest;
import com.msbanking.users_service.dto.request.UpdateUserRequest;
import com.msbanking.users_service.dto.response.UserResponse;

import java.util.List;

public interface UserService {
    
    UserResponse createUser(CreateUserRequest request);
    
    UserResponse getUserById(Long id);
    
    UserResponse getUserByUsername(String username);
    
    List<UserResponse> getAllUsers();
    
    UserResponse updateUser(Long id, UpdateUserRequest request);
    
    void deleteUser(Long id);
}
