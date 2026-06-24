package com.tiltedhat.devspace.dto;

import com.tiltedhat.devspace.entity.Post;
import java.time.LocalDateTime;

public record PostResponse(
        Long id,
        String title,
        String slug,
        String content,
        UserResponse author,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static PostResponse fromEntity(Post post) {
        return new PostResponse(
                post.getId(),
                post.getTitle(),
                post.getSlug(),
                post.getContent(),
                UserResponse.fromEntity(post.getAuthor()),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }
}