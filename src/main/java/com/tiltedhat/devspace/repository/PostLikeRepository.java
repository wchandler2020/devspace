package com.tiltedhat.devspace.repository;

import com.tiltedhat.devspace.entity.PostLike;
import com.tiltedhat.devspace.entity.PostLikeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, PostLikeId> {
    boolean existsByPostSlugAndUserUsername(String slug, String username);
    void deleteByPostSlugAndUserUsername(String slug, String username);
    long countByPostSlug(String slug);
}
