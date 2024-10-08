package io.blog.devlog.domain.like.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Embeddable
public class PostLikeId implements Serializable {
    @Column(name = "post_id")
    private Long postId;
    @Column(name = "user_id")
    private Long userId;

    public PostLikeId() {}

    public PostLikeId(Long postId, Long userId) {
        this.postId = postId;
        this.userId = userId;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PostLikeId that = (PostLikeId) o;
        return Objects.equals(postId, that.postId) &&
                Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(postId, userId);
    }
}
