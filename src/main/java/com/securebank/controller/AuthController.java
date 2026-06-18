package com.securebank.controller;

import com.securebank.dto.request.RegisterRequest;
import com.securebank.dto.response.AuthResponse;
import com.securebank.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.securebank.dto.request.LoginRequest;
import com.securebank.dto.response.LoginResponse;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor

public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request){
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request){
        return ResponseEntity.ok(authService.login(request));
    }

}
