package com.tiltedhat.devspace.service;

import com.tiltedhat.devspace.entity.Post;
import com.tiltedhat.devspace.entity.User;
import com.tiltedhat.devspace.repository.PostRepository;
import com.tiltedhat.devspace.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Transactional
    public Post createPost(PostRequest request){
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();

        User author = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new UsernameNotFoundException("Authenticated User not found."));

        String slug = generateSlug(request.getTitle());
        
        if(postRepository.findBySlug(slug).isPresent()){
            slug += "_"+System.currentTimeMillis() % 10000;
        }
        
        Post post = new Post();
        
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setSlug(slug);
        post.setAuthor(author);

        return postRepository.save(post);
    }

    private String generateSlug(String title) {
        return title.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-8\\s-]", "") // Remove special characters
                .replaceAll("\\s+", "-")         // Replace spaces with hyphens
                .replaceAll("-+", "-")           // Collapse consecutive hyphens
                .replaceAll("^-|-$", "");        // Trim leading/trailing hyphens
    }
}
