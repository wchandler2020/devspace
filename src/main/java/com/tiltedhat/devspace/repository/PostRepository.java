package com.tiltedhat.devspace.repository;

import com.tiltedhat.devspace.entity.Post;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tools.jackson.databind.JacksonSerializable;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    // Custom finder method to locate a blog post by the url slug
    @EntityGraph(attributePaths = {"author"})
    Optional<Post> findBySlug(String slug);

    // Override findAll to eagerly fetch the author association
    @EntityGraph(attributePaths = {"author"})
    List<Post> findAll();



}
