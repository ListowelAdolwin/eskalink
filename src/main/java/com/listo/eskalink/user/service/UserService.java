package com.listo.eskalink.user.service;

import com.listo.eskalink.common.exception.ResourceNotFoundException;
import com.listo.eskalink.common.exception.ValidationException;
import com.listo.eskalink.security.JwtUtil;
import com.listo.eskalink.user.dto.*;
import com.listo.eskalink.user.entity.User;
import com.listo.eskalink.user.mapper.UserMapper;
import com.listo.eskalink.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final EmailService emailService;

    @Value("${app.base-url}")
    private String baseUrl;

    @Transactional
    public UserDto signup(SignupRequest request) {
        log.info("Processing signup request for email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ValidationException("Email already exists");
        }

        User user = userMapper.signupRequestToUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        String verificationToken = jwtUtil.generateVerificationToken(request.getEmail(), user.getId());
        user.setVerificationToken(verificationToken);
        user.setVerificationTokenExpiresAt(LocalDateTime.now().plusHours(1));
        user.setIsVerified(false);

        user = userRepository.save(user);

        String verificationLink = baseUrl + "/api/auth/verify-email?token=" + verificationToken;
        emailService.sendVerificationEmail(user.getEmail(), user.getName(), verificationLink);

        log.info("User created successfully with ID: {}", user.getId());
        return userMapper.userToUserDto(user);
    }

    @Transactional
    public void verifyEmail(String token) {
        log.info("Processing email verification for token: {}", token);

        if (!jwtUtil.validateVerificationToken(token)) {
            User user = userRepository.findByVerificationToken(token)
                    .orElseThrow(() -> new ValidationException("Invalid or malformed token"));

            if (user.getIsVerified()) {
                throw new ValidationException("Email has already been verified");
            }

            String newToken = jwtUtil.generateVerificationToken(user.getEmail(), user.getId());
            user.setVerificationToken(newToken);
            user.setVerificationTokenExpiresAt(LocalDateTime.now().plusHours(1));
            userRepository.save(user);

            String verificationLink = baseUrl + "/api/auth/verify-email?token=" + newToken;
            emailService.sendVerificationEmail(user.getEmail(), user.getName(), verificationLink);

            throw new ValidationException("Token expired. New verification email sent");
        }

        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new ValidationException("Invalid token"));

        if (user.getIsVerified()) {
            throw new ValidationException("Email has already been verified");
        }

        user.setIsVerified(true);
        user.setVerificationToken(null);
        user.setVerificationTokenExpiresAt(null);
        userRepository.save(user);

        log.info("Email verified successfully for user: {}", user.getEmail());
    }

    public LoginResponse login(LoginRequest request) {
        log.info("Processing login request for email: {}", request.getEmail());

        try {
//            Authentication authentication = authenticationManager.authenticate(
//                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
//            );

            UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
            String token = jwtUtil.generateToken(userDetails);

            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new ValidationException("User not found"));

            if (!user.getIsVerified()) {
                throw new ValidationException("Please verify your email before logging in");
            }

            log.info("Login successful for user: {}", request.getEmail());

            return LoginResponse.builder()
                    .token(token)
                    .userId(user.getId())
                    .email(user.getEmail())
                    .name(user.getName())
                    .role(user.getRole())
                    .build();

        } catch (Exception e) {
            log.error("Login failed for email: {}", request.getEmail());
            throw new ValidationException("Invalid credentials");
        }
    }

    public UserDto getCurrentUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return userMapper.userToUserDto(user);
    }

    @Transactional
    public void cleanupExpiredTokens() {
        userRepository.deleteByVerificationTokenExpiresAtBeforeAndIsVerifiedFalse(LocalDateTime.now());
    }
}
