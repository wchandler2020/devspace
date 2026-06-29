package com.tiltedhat.devspace.repository;

import com.tiltedhat.devspace.entity.Post;
import com.tiltedhat.devspace.entity.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import tools.jackson.databind.JacksonSerializable;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    @EntityGraph(attributePaths = {"author", "tags", "comments", "comments.author"})
    Optional<Post> findBySlug(String slug);

    @EntityGraph(attributePaths = {"author", "comments", "tags"})
    Page<Post> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"author", "comments", "tags"})
    Page<Post> findByTags_Slug(String slug, Pageable pageable);

    // ADDED: For anonymous users viewing the general feed (no tag filter)
    @EntityGraph(attributePaths = {"author", "comments", "tags"})
    Page<Post> findByStatus(PostStatus status, Pageable pageable);

    // ADDED: For anonymous users filtering the feed by a specific tag
    @EntityGraph(attributePaths = {"author", "comments", "tags"})
    Page<Post> findByStatusAndTags_Slug(PostStatus status, String tagSlug, Pageable pageable);

    // ADDED: For logged-in users viewing the general feed (no tag filter)
    @EntityGraph(attributePaths = {"author", "comments", "tags"})
    @Query("SELECT p FROM Post p WHERE p.status = 'PUBLISHED' OR (p.status = 'DRAFT' AND p.author.username = :username)")
    Page<Post> findFeedForUser(String username, Pageable pageable);

    // 4. Fetch tag-filtered feed for a logged-in user (Already perfect!)
    @EntityGraph(attributePaths = {"author", "comments", "tags"})
    @Query("SELECT p FROM Post p JOIN p.tags t WHERE (p.status = 'PUBLISHED' OR (p.status = 'DRAFT' AND p.author.username = :username)) AND t.slug = :tagSlug")
    Page<Post> findFeedForUserAndTag(String username, String tagSlug, Pageable pageable);

    @EntityGraph(attributePaths = {"author", "tags", "comments"})
    Page<Post> findByAuthorUsernameAndStatus(String username, PostStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"author", "tags", "comments", "comments.author"})
    Page<Post> findByAuthorUsername(String username, Pageable pageable);
}
