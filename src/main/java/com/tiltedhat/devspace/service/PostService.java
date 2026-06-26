package com.tiltedhat.devspace.service;

import com.tiltedhat.devspace.entity.Post;
import com.tiltedhat.devspace.entity.PostLike;
import com.tiltedhat.devspace.entity.PostStatus;
import com.tiltedhat.devspace.entity.User;
import com.tiltedhat.devspace.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.tiltedhat.devspace.dto.PostRequest;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final CommentRepository commentRepository;
    private final PostLikeRepository postLikeRepository;

    @Transactional
    public Post createPost(PostRequest request){
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();

        User author = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new UsernameNotFoundException("Authenticated User not found."));

        String slug = generateSlug(request.title());
        
        if(postRepository.findBySlug(slug).isPresent()){
            slug += "_"+System.currentTimeMillis() % 10000;
        }

        Post post = new Post();
        
        post.setTitle(request.title());
        post.setContent(request.content());
        post.setSlug(slug);
        post.setAuthor(author);

        post.setTags(processTags(request.tags()));

        if (request.status() != null) {
            try {
                // Convert the raw JSON string ("DRAFT" or "PUBLISHED") into the official PostStatus Enum
                post.setStatus(com.tiltedhat.devspace.entity.PostStatus.valueOf(request.status().trim().toUpperCase()));
            } catch (IllegalArgumentException e) {
                // Fallback safety if someone types a typo string like "DFRFT" instead of "DRAFT"
                post.setStatus(PostStatus.DRAFT);
            }
        } else {
            post.setStatus(PostStatus.DRAFT);
        }

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
        post.setTitle(request.title());
        post.setContent(request.content());
        post.setTags(processTags(request.tags()));

        // Regenerate slug if title changed
        String newSlug = generateSlug(request.title());
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

    @Transactional
    public void likePost(String slug) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        Post post = postRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Post not found with slug: " + slug));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        if (postLikeRepository.existsByPostSlugAndUserUsername(slug, username)) {
            throw new RuntimeException("You have already liked this post.");
        }

        PostLike like = new PostLike();
        like.getPost(); // set via setters
        like.setPost(post);
        like.setUser(user);

        postLikeRepository.save(like);
    }

    @Transactional
    public void unlikePost(String slug) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        if (!postLikeRepository.existsByPostSlugAndUserUsername(slug, username)) {
            throw new RuntimeException("You have not liked this post.");
        }

        postLikeRepository.deleteByPostSlugAndUserUsername(slug, username);
    }

    public long getLikeCount(String slug) {
        return postLikeRepository.countByPostSlug(slug);
    }

    private String generateSlug(String title) {
        return title.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-8\\s-]", "") // Remove special characters
                .replaceAll("\\s+", "-")         // Replace spaces with hyphens
                .replaceAll("-+", "-")           // Collapse consecutive hyphens
                .replaceAll("^-|-$", "");        // Trim leading/trailing hyphens
    }

    // Fetch every single blog post in the database

    // Replace your old getAllPosts method with this one:
    public Page<Post> getAllPosts(int page, int size, String tagSlug) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        // 1. Check if a user is securely logged in or browsing anonymously
        org.springframework.security.core.Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser");

        // 2. Logic for Authenticated Logged-In Users
        if (isAuthenticated) {
            String currentUsername = auth.getName();
            // If they want a specific tag, fetch all published items + their own drafts matching that tag
            if (tagSlug != null && !tagSlug.isBlank()) {
                return postRepository.findFeedForUserAndTag(currentUsername, tagSlug.trim().toLowerCase(), pageable);
            }
            // Otherwise, fetch their personalized homepage feed
            return postRepository.findFeedForUser(currentUsername, pageable);

        } else {
            // 3. Logic for Anonymous Public Visitors (Strictly see PUBLISHED content only)
            if (tagSlug != null && !tagSlug.isBlank()) {
                return postRepository.findByStatusAndTags_Slug(PostStatus.PUBLISHED, tagSlug.trim().toLowerCase(), pageable);
            }
            return postRepository.findByStatus(PostStatus.PUBLISHED, pageable);
        }
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

    // Inside com.tiltedhat.devspace.service.PostService

    @Transactional
    public void deleteComment(Long commentId) {
        // 1. Find the comment
        com.tiltedhat.devspace.entity.Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found with ID: " + commentId));

        // 2. Get the currently authenticated username
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();

        // 3. Authorization Check
        String commentAuthor = comment.getAuthor().getUsername();
        String postOwner = comment.getPost().getAuthor().getUsername();

        boolean isCommentAuthor = commentAuthor.equals(currentUsername);
        boolean isPostOwner = postOwner.equals(currentUsername);

        System.out.println("=== DELETION DEBUG ===");
        System.out.println("Logged-in User: [" + currentUsername + "]");
        System.out.println("Comment Author: [" + commentAuthor + "]");
        System.out.println("Post Owner: [" + postOwner + "]");
        System.out.println("======================");

        if (!isCommentAuthor && !isPostOwner) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "You are not authorized to delete this comment."
            );
        }

        // 4. Delete the comment
        commentRepository.delete(comment);
    }



}
