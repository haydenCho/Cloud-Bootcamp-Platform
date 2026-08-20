package com.solcho.bootcamp.content.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
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
 * docs/db-schema.md 의 content 테이블 (일반 학습 본문).
 * body 는 관리자 WYSIWYG 로 작성되는 HTML(6단계). 4단계에서는 DataInitializer 로 시드하고,
 * 렌더링 전 프론트에서 sanitize 한다.
 */
@Entity
@Table(name = "content")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Content {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** GENERAL 타입 unit 에 연결되는 FK (unit.id). */
    @Column(name = "unit_id", nullable = false)
    private Long unitId;

    @Column(name = "title", length = 200, nullable = false)
    private String title;

    @Lob
    @Column(name = "body", nullable = false, columnDefinition = "LONGTEXT")
    private String body;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    private Content(Long unitId, String title, String body) {
        this.unitId = unitId;
        this.title = title;
        this.body = body;
    }

    /** 시드 본문 갱신용(관리자 에디터 전까지). 내용이 바뀐 경우에만 호출한다. */
    public void updateBody(String title, String body) {
        this.title = title;
        this.body = body;
    }
}
