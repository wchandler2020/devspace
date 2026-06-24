package com.tiltedhat.devspace.dto;

import com.tiltedhat.devspace.entity.User;

public record UserResponse(
        Long id,
        String username,
        String email
) {
    public static UserResponse fromEntity(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail()
        );
    }
}