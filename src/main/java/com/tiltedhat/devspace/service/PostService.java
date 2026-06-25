package com.tiltedhat.devspace.service;

import com.tiltedhat.devspace.entity.Post;
import com.tiltedhat.devspace.entity.User;
import com.tiltedhat.devspace.repository.PostRepository;
import com.tiltedhat.devspace.repository.TagRepository;
import com.tiltedhat.devspace.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    private final TagRepository tagRepository;

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

        post.setTags(processTags(request.getTags()));

        return postRepository.save(post);
    }

    @Transactional
    public Post updatePost(String slug, PostRequest request){
        // 1. Find the post
        Post post = postRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Post not found with slug: " + slug));

        // 2. Get the currently logged-in username
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();

        // 3. Strict Check: Does the logged-in user own this post?
        if (!post.getAuthor().getUsername().equals(currentUsername)) {
            throw new org.springframework.security.access.AccessDeniedException("You are not authorized to update this post.");
        }

        // 4. Update the values
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setTags(processTags(request.getTags()));

        // Regenerate slug if title changed
        String newSlug = generateSlug(request.getTitle());
        if (!newSlug.equals(post.getSlug()) && postRepository.findBySlug(newSlug).isPresent()) {
            newSlug += "-" + (System.currentTimeMillis() % 10000);
        }
        post.setSlug(newSlug);

        return postRepository.save(post);
    }

    @Transactional
    public void deletePost(String slug){
        // 1. Find the post
        Post post = postRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Post not found with slug: " + slug));

        // 2. Get the currently logged-in username
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();

        // 3. Strict Check: Does the logged-in user own this post?
        if (!post.getAuthor().getUsername().equals(currentUsername)) {
            throw new org.springframework.security.access.AccessDeniedException("You are not authorized to delete this post.");
        }

        // 4. Delete from database
        postRepository.delete(post);
    }

    private String generateSlug(String title) {
        return title.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-8\\s-]", "") // Remove special characters
                .replaceAll("\\s+", "-")         // Replace spaces with hyphens
                .replaceAll("-+", "-")           // Collapse consecutive hyphens
                .replaceAll("^-|-$", "");        // Trim leading/trailing hyphens
    }

    // Fetch every single blog post in the database

    public Page<Post> getAllPosts(int page, int size, String tagSlug) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        // If a tag parameter is provided, filter by it!
        if (tagSlug != null && !tagSlug.isBlank()) {
            return postRepository.findByTags_Slug(tagSlug.trim().toLowerCase(), pageable);
        }

        // Otherwise, default to returning everything
        return postRepository.findAll(pageable);
    }

    // Fetch a single blog post using its unique text URL slug
    public Post getPostBySlug(String slug) {
        return postRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Blog post not found with slug: " + slug));
    }

    private java.util.Set<com.tiltedhat.devspace.entity.Tag> processTags(java.util.Set<String> tagNames) {
        java.util.Set<com.tiltedhat.devspace.entity.Tag> managedTags = new java.util.HashSet<>();
        if (tagNames == null || tagNames.isEmpty()) {
            return managedTags;
        }

        for (String name : tagNames) {
            String cleanName = name.trim().toLowerCase();
            if (cleanName.isEmpty()) continue;

            com.tiltedhat.devspace.entity.Tag tag = tagRepository.findByName(cleanName)
                    .orElseGet(() -> {
                        com.tiltedhat.devspace.entity.Tag newTag = new com.tiltedhat.devspace.entity.Tag();
                        newTag.setName(cleanName);

                        // ⬇️ Fix: Set the slug right here using your helper method!
                        newTag.setSlug(generateSlug(cleanName));

                        return tagRepository.save(newTag);
                    });
            managedTags.add(tag);
        }
        return managedTags;
    }

}
