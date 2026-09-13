package com.arvind.user.service;

import com.arvind.user.dto.AuthResponse;
import com.arvind.user.dto.LoginRequest;
import com.arvind.user.dto.LogoutRequest;
import com.arvind.user.dto.RegisterRequest;
import com.arvind.user.dto.UserResponse;
import java.util.List;

public interface AuthService {

    void register(RegisterRequest request);

    AuthResponse login(LoginRequest request);
    void logout(LogoutRequest request);
    List<UserResponse> getAllUsers();
}