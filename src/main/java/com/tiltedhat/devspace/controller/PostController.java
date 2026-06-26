package com.tiltedhat.devspace.controller;

import com.tiltedhat.devspace.dto.PostRequest;
import com.tiltedhat.devspace.dto.PostResponse;
import com.tiltedhat.devspace.entity.Post;

import com.tiltedhat.devspace.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;

    @PostMapping
    public ResponseEntity<PostResponse> createPost(@Valid @RequestBody PostRequest request){
        return ResponseEntity.ok(PostResponse.fromEntity(postService.createPost(request)));
    }

    // Update this endpoint in your PostController.java
    @GetMapping
    public ResponseEntity<org.springframework.data.domain.Page<PostResponse>> getAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String tag
    ) {
        // 1. Pass page/size query parameters into your service class
        org.springframework.data.domain.Page<Post> postPage = postService.getAllPosts(page, size, tag);

        // 2. Map the inner stream of entities over to our clean PostResponse DTO record
        org.springframework.data.domain.Page<PostResponse> responses = postPage.map(PostResponse::fromEntity);

        return ResponseEntity.ok(responses);
    }

    // Fetch a single post by the its unique URL slugs
    @GetMapping("/{slug}")
    public ResponseEntity<PostResponse> getPostBySlug(@PathVariable String slug){
        return ResponseEntity.ok(PostResponse.fromEntity(postService.getPostBySlug(slug)));
    }

    // Update an existing post by slug
    @PutMapping("/{slug}")
    public ResponseEntity<PostResponse> updatePost(@PathVariable String slug,@Valid @RequestBody PostRequest request) {
        return ResponseEntity.ok(PostResponse.fromEntity(postService.updatePost(slug, request)));
    }

    // Delete a post by slug
    @DeleteMapping("/{slug}")
    public ResponseEntity<Void> deletePost(@PathVariable String slug) {
        postService.deletePost(slug);
        return ResponseEntity.noContent().build(); // Returns a 204 No Content on success
    }

    @PostMapping("/{slug}/like")
    public ResponseEntity<Void> likePost(@PathVariable String slug) {
        postService.likePost(slug);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{slug}/like")
    public ResponseEntity<Void> unlikePost(@PathVariable String slug) {
        postService.unlikePost(slug);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{slug}/likes")
    public ResponseEntity<Long> getLikeCount(@PathVariable String slug) {
        return ResponseEntity.ok(postService.getLikeCount(slug));
    }
}
