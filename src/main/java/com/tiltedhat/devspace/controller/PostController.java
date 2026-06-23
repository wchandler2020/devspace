package com.tiltedhat.devspace.controller;

import com.tiltedhat.devspace.entity.Post;
import com.tiltedhat.devspace.service.PostRequest;
import com.tiltedhat.devspace.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;

    @PostMapping
    public ResponseEntity<Post> createPost(@Valid @RequestBody PostRequest postRequest){
        Post savedPost = postService.createPost(postRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedPost);
    }

    @GetMapping
    public ResponseEntity<List<Post>> getAllPosts(){
        return ResponseEntity.ok(postService.getAllPosts());
    }

    // Fetch a single post by the its unique URL slugs
    @GetMapping("/{slug}")
    public ResponseEntity<Post> getPostBySlug(@PathVariable String slug){
        return ResponseEntity.ok(postService.getPostBySlug(slug));
    }

    // Update an existing post by slug
    @PutMapping("/{slug}")
    public ResponseEntity<Post> updatePost(@PathVariable String slug, @RequestBody PostRequest request) {
        return ResponseEntity.ok(postService.updatePost(slug, request));
    }

    // Delete a post by slug
    @DeleteMapping("/{slug}")
    public ResponseEntity<Void> deletePost(@PathVariable String slug) {
        postService.deletePost(slug);
        return ResponseEntity.noContent().build(); // Returns a 204 No Content on success
    }
}
