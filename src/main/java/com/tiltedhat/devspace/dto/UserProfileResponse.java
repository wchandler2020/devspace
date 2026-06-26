package com.tiltedhat.devspace.dto;

import com.tiltedhat.devspace.entity.Post;
import com.tiltedhat.devspace.entity.User;

import java.time.LocalDateTime;
import java.util.List;

public record UserProfileResponse (Long id,
                                   String username,
                                   LocalDateTime memberSince,
                                   List<PostResponse> posts
){
    public static UserProfileResponse fromEntity(User user, List<Post> posts) {
        return new UserProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getCreatedAt(),
                posts.stream().map(PostResponse::fromEntity).toList()
        );
    }
}
