package com.tiltedhat.devspace.entity;

import jakarta.persistence.Embeddable;
import lombok.Data;

import java.io.Serializable;

@Embeddable
@Data
public class PostLikeId implements Serializable {
    private Long postId;
    private Long userId;
}
