package com.tiltedhat.devspace.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommentRequest(
        @NotBlank(message = "Comment text cannot be blank")
        @Size(min = 2, max = 500, message = "Comment must be between 2 and 500 characters")
        String content
) {}
