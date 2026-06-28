package com.tiltedhat.devspace.controller;

import com.tiltedhat.devspace.dto.CommentRequest;
import com.tiltedhat.devspace.dto.CommentResponse;
import com.tiltedhat.devspace.entity.Comment;
import com.tiltedhat.devspace.service.CommentService;
import com.tiltedhat.devspace.service.PostService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts/{slug}/comments")
public class CommentController {
    private final CommentService commentService;
    private final PostService postService;

    public CommentController(CommentService commentService, PostService postService) {
        this.commentService = commentService;
        this.postService = postService;
    }

//    public CommentController(CommentService commentService) {
//        this.commentService = commentService;
//    }

    @GetMapping
    public ResponseEntity<Page<CommentResponse>> getComments(
            @PathVariable String slug,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<CommentResponse> comments = commentService.getCommentsForPost(slug, page, size)
                .map(CommentResponse::fromEntity);
        return ResponseEntity.ok(comments);
    }

    @PostMapping
    public ResponseEntity<CommentResponse> addComment(
            @PathVariable String slug,
            @Valid @RequestBody CommentRequest request) {
        Comment comment = commentService.addComment(slug, request);
        return ResponseEntity.ok(CommentResponse.fromEntity(comment));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long id) {
        postService.deleteComment(id);
        return ResponseEntity.noContent().build(); // Returns a clean 204 No Content response upon success
    }
}
