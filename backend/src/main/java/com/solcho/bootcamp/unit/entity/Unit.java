package com.solcho.bootcamp.unit.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * docs/db-schema.md 의 unit 테이블 매핑 (학습 단원).
 */
@Entity
@Table(name = "unit")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Unit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 슬러그 (예: linux, linux-practice). 프론트 라우팅 /units/:code 및 아이콘 경로 기준. */
    @Column(name = "code", length = 50, nullable = false, unique = true)
    private String code;

    /** 화면 표시명 (예: "리눅스"). */
    @Column(name = "name", length = 100, nullable = false)
    private String name;

    /** 로드맵상 같은 그룹으로 묶기 위한 키 (예: linux → 리눅스/리눅스(실습) 공통). */
    @Column(name = "group_code", length = 50, nullable = false)
    private String groupCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 10, nullable = false)
    private UnitType type;

    /** 아이콘 이미지 경로 (프론트에서 바로 쓰는 웹 경로, 예: /assets/imgs/roadmap/linux.png). */
    @Column(name = "icon_image_path", length = 255)
    private String iconImagePath;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Builder
    private Unit(String code, String name, String groupCode, UnitType type,
                 String iconImagePath, int sortOrder) {
        this.code = code;
        this.name = name;
        this.groupCode = groupCode;
        this.type = type;
        this.iconImagePath = iconImagePath;
        this.sortOrder = sortOrder;
    }
}
