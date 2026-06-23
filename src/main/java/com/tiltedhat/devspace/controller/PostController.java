package com.tiltedhat.devspace.controller;

import com.tiltedhat.devspace.entity.Post;
import com.tiltedhat.devspace.service.PostRequest;
import com.tiltedhat.devspace.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
