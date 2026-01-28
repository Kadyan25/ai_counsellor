package com.aicounsellor.backend.auth;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.aicounsellor.backend.auth.dto.LoginRequest;
import com.aicounsellor.backend.auth.dto.SignupRequest;
import com.aicounsellor.backend.security.JwtService;
import com.aicounsellor.backend.users.User;
import com.aicounsellor.backend.users.UserRepository;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public AuthService(UserRepository userRepository, JwtService jwtService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    public String signup(SignupRequest request) {
        String email = request.getEmail().toLowerCase().trim();

        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already registered");
        }

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setName(request.getName());
        user.setEmail(email);
        user.setPasswordHash(encoder.encode(request.getPassword()));
        user.setCreatedAt(OffsetDateTime.now());

        userRepository.save(user);

        return jwtService.generateToken(user.getId());
    }

    public String login(LoginRequest request) {
        String email = request.getEmail().toLowerCase().trim();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!encoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid credentials");
        }

        return jwtService.generateToken(user.getId());
    }
}
