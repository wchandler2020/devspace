package com.tiltedhat.devspace.repository;

import com.tiltedhat.devspace.entity.Comment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    // Efficiently fetch all comments for a post along with their author names in one query
    @EntityGraph(attributePaths = {"author"})
    List<Comment> findByPostSlugOrderByCreatedAtDesc(String slug);

    // This fetches the comment, its author, the post, and the post's author all at once
    @EntityGraph(attributePaths = {"author", "post", "post.author"})
    Optional<Comment> findById(Long id);
}
