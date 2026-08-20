package com.solcho.bootcamp.community.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * docs/db-schema.md 의 community_comment 테이블 (댓글 / 답글).
 * parent_comment_id 가 NULL 이면 최상위 댓글, 값이 있으면 그 댓글에 대한 답글(1단계 깊이만 허용).
 */
@Entity
@Table(name = "community_comment")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommunityComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** NULL = 최상위 댓글, 값 있음 = 답글. */
    @Column(name = "parent_comment_id")
    private Long parentCommentId;

    @Column(name = "body", columnDefinition = "TEXT", nullable = false)
    private String body;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    private CommunityComment(Long postId, Long userId, Long parentCommentId, String body) {
        this.postId = postId;
        this.userId = userId;
        this.parentCommentId = parentCommentId;
        this.body = body;
    }

    public boolean isReply() {
        return this.parentCommentId != null;
    }

    public void update(String body) {
        this.body = body;
    }
}
