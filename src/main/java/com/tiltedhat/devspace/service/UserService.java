package com.tiltedhat.devspace.service;

import com.tiltedhat.devspace.dto.UserProfileResponse;
import com.tiltedhat.devspace.entity.Post;
import com.tiltedhat.devspace.entity.PostStatus;
import com.tiltedhat.devspace.entity.User;
import com.tiltedhat.devspace.repository.PostRepository;
import com.tiltedhat.devspace.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(String username) {
        // 1. Find the user or throw a clean error
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        // 2. Fetch only their PUBLISHED posts, newest first
        List<Post> posts = postRepository.findByAuthorUsernameAndStatus(
                username,
                PostStatus.PUBLISHED,
                PageRequest.of(0, 10, Sort.by("createdAt").descending())
        ).getContent();

        // 3. Build and return the response
        return UserProfileResponse.fromEntity(user, posts);
    }
}
