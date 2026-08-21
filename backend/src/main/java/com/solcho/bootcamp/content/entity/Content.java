package com.solcho.bootcamp.content.entity;

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
 * docs/db-schema.md 의 content 테이블 (학습 본문의 상위 묶음).
 * 8단계 개선: 통짜 body 를 제거하고, 실제 본문은 content_chapter(챕터) 로 분리했다.
 * content 는 단원당 1행으로 제목만 갖고, 챕터들이 이 content 를 참조한다.
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

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    private Content(Long unitId, String title) {
        this.unitId = unitId;
        this.title = title;
    }

    /** 시드 제목 갱신용(관리자 에디터 전까지). 내용이 바뀐 경우에만 호출한다. */
    public void updateTitle(String title) {
        this.title = title;
    }
}
