package com.tiltedhat.devspace.controller;

import com.tiltedhat.devspace.dto.CommentRequest;
import com.tiltedhat.devspace.dto.CommentResponse;
import com.tiltedhat.devspace.entity.Comment;
import com.tiltedhat.devspace.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts/{slug}/comments")
public class CommentController {
    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping
    public ResponseEntity<List<CommentResponse>> getComments(@PathVariable String slug) {
        List<CommentResponse> comments = commentService.getCommentsForPost(slug).stream()
                .map(CommentResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(comments);
    }

    @PostMapping
    public ResponseEntity<CommentResponse> addComment(
            @PathVariable String slug,
            @Valid @RequestBody CommentRequest request) {
        Comment comment = commentService.addComment(slug, request);
        return ResponseEntity.ok(CommentResponse.fromEntity(comment));
    }
}
