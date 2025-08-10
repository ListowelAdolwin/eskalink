package com.listo.eskalink.user.dto;

import com.listo.eskalink.user.enums.UserRole;
import com.listo.eskalink.user.validation.ValidFullName;
import com.listo.eskalink.user.validation.ValidPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SignupRequest {

    @NotBlank(message = "Name is required")
    @ValidFullName
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    private String email;

    @NotBlank(message = "Password is required")
    @ValidPassword
    private String password;

    @NotNull(message = "Role is required")
    private UserRole role;
}
