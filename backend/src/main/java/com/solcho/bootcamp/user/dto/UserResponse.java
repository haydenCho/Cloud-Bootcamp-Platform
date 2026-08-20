package com.solcho.bootcamp.user.dto;

import com.solcho.bootcamp.user.entity.Role;
import com.solcho.bootcamp.user.entity.User;
import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String loginId,
        String nickname,
        String profileImageUrl,
        Role role,
        LocalDateTime createdAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getLoginId(),
                user.getNickname(),
                user.getProfileImageUrl(),
                user.getRole(),
                user.getCreatedAt()
        );
    }
}
