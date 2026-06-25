package com.tiltedhat.devspace.dto;

import com.tiltedhat.devspace.entity.Comment;
import java.time.LocalDateTime;

public record CommentResponse(
        Long id,
        String content,
        UserResponse author,
        LocalDateTime createdAt
) {
    public static CommentResponse fromEntity(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getContent(),
                UserResponse.fromEntity(comment.getAuthor()), // Safe author metadata
                comment.getCreatedAt()
        );
    }
}