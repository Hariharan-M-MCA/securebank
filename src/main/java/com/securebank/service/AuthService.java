package com.securebank.service;

import com.securebank.dto.request.LoginRequest;
import com.securebank.dto.request.RegisterRequest;
import com.securebank.dto.response.AuthResponse;
import com.securebank.dto.response.LoginResponse;
import com.securebank.entity.User;
import com.securebank.repository.UserRepository;
import com.securebank.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import com.securebank.exception.UserNotFoundException;
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public LoginResponse login(LoginRequest request){
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(()-> new UserNotFoundException("User not found"));

        String token = jwtUtil.generateToken(user.getEmail(),user.getRole());
        return new LoginResponse(token, user.getEmail(),user.getRole());
    }


    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            return new AuthResponse("Email already exists", false);
        }

        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhoneNumber(request.getPhoneNumber());
        user.setRole("USER");
        user.setCreatedAt(LocalDateTime.now());
        user.setIsActive(true);

        userRepository.save(user);
        return new AuthResponse("Registration successful", true);
    }
}
