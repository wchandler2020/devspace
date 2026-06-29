package com.tiltedhat.devspace.dto;

import com.tiltedhat.devspace.entity.Post;
import com.tiltedhat.devspace.entity.Tag;
import java.time.LocalDateTime;
import java.util.List;

public record PostResponse(
        Long id,
        String title,
        String slug,
        String content,
        UserResponse author,
        List<CommentResponse> comments,
        List<String> tags,
        Long viewCount,
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
                post.getComments() != null ? post.getComments().stream().map(CommentResponse::fromEntity).toList() : List.of(),
                post.getTags() != null ? post.getTags().stream().map(Tag::getName).toList() : List.of(),
                post.getViewCount(),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }
}