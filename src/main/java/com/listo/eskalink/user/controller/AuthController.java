package com.listo.eskalink.user.controller;

import com.listo.eskalink.common.dto.BaseResponse;
import com.listo.eskalink.security.CustomUserDetails;
import com.listo.eskalink.user.dto.*;
import com.listo.eskalink.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "User authentication and registration endpoints")
public class AuthController {

    private final UserService userService;

    @PostMapping("/signup")
    @Operation(summary = "User Registration", description = "Register a new user as company or applicant")
    public ResponseEntity<BaseResponse<UserDto>> signup(@Valid @RequestBody SignupRequest request) {
        log.info("Signup request received for email: {}", request.getEmail());

        UserDto userDto = userService.signup(request);
        BaseResponse<UserDto> response = BaseResponse.success(
                "User registered successfully. Please check your email for verification link.",
                userDto
        );

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/verify-email")
    @Operation(summary = "Email Verification", description = "Verify user email using verification token")
    public ResponseEntity<BaseResponse<String>> verifyEmail(
            @Parameter(description = "Email verification token", required = true)
            @RequestParam String token) {
        log.info("Email verification request received");

        userService.verifyEmail(token);
        BaseResponse<String> response = BaseResponse.success(
                "Email verified successfully. You can now log in.",
                null
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    @Operation(summary = "User Login", description = "Authenticate user and return JWT token")
    public ResponseEntity<BaseResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request received for email: {}", request.getEmail());

        LoginResponse loginResponse = userService.login(request);
        BaseResponse<LoginResponse> response = BaseResponse.success(
                "Login successful",
                loginResponse
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    @Operation(summary = "Get Current User", description = "Get current authenticated user information")
    public ResponseEntity<BaseResponse<UserDto>> getCurrentUser(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        UserDto userDto = userService.getCurrentUser(userDetails.getUserId());
        BaseResponse<UserDto> response = BaseResponse.success(
                "User information retrieved successfully",
                userDto
        );

        return ResponseEntity.ok(response);
    }
}
