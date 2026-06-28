package com.tiltedhat.devspace.service;

import com.tiltedhat.devspace.dto.CommentRequest;
import com.tiltedhat.devspace.entity.Comment;
import com.tiltedhat.devspace.entity.Post;
import com.tiltedhat.devspace.entity.User;
import com.tiltedhat.devspace.repository.CommentRepository;
import com.tiltedhat.devspace.repository.PostRepository;
import com.tiltedhat.devspace.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public CommentService(CommentRepository commentRepository, PostRepository postRepository, UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public Page<Comment> getCommentsForPost(String slug, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return commentRepository.findByPostSlugOrderByCreatedAtDesc(slug, pageable);
    }

    @Transactional
    public Comment addComment(String slug, CommentRequest request) {
        Post post = postRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Blog post not found with slug: " + slug));

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Logged-in user context missing"));

        Comment comment = new Comment();
        comment.setContent(request.content());
        comment.setPost(post);
        comment.setAuthor(currentUser);

        return commentRepository.save(comment);
    }
}
