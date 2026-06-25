package com.tiltedhat.devspace.repository;

import com.tiltedhat.devspace.entity.Comment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    // Efficiently fetch all comments for a post along with their author names in one query
    @EntityGraph(attributePaths = {"author"})
    List<Comment> findByPostSlugOrderByCreatedAtDesc(String slug);
}
