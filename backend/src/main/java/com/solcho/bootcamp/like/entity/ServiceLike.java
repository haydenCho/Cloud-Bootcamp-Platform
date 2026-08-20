package com.solcho.bootcamp.like.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * docs/db-schema.md 의 service_like 테이블 (서비스 좋아요).
 * 사용자당 1행 — 좋아요를 누른 사용자만 존재하며, 취소 시 행을 삭제한다.
 * 총 개수는 COUNT(*) 로 계산한다(저장하지 않음).
 */
@Entity
@Table(name = "service_like")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ServiceLike {

    /** user.id (PK 겸 FK). 별도 auto id 없이 사용자당 1행. */
    @Id
    @Column(name = "user_id")
    private Long userId;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public ServiceLike(Long userId) {
        this.userId = userId;
    }
}
