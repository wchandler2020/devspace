package com.tiltedhat.devspace.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PostRequest(
        @NotBlank(message = "Title cannot be blank")
        @Size(min = 3, max = 100, message = "Title must be between 3 and 100 characters")
        String title,

        @NotBlank(message = "Content cannot be blank")
        @Size(min = 10, message = "Content must be at least 10 characters long")
        String content
) {}
