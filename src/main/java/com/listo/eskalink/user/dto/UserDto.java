package com.listo.eskalink.user.dto;

import com.listo.eskalink.user.enums.UserRole;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class UserDto {
    private UUID id;
    private String name;
    private String email;
    private UserRole role;
    private Boolean isVerified;
    private LocalDateTime createdAt;
}
