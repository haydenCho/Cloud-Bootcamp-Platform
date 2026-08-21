package com.solcho.bootcamp.content.service;

import com.solcho.bootcamp.activity.service.ActivityLogService;
import com.solcho.bootcamp.common.exception.ApiException;
import com.solcho.bootcamp.content.dto.ChapterDetailResponse;
import com.solcho.bootcamp.content.dto.ChapterSummaryResponse;
import com.solcho.bootcamp.content.entity.Content;
import com.solcho.bootcamp.content.entity.ContentChapter;
import com.solcho.bootcamp.content.repository.ContentChapterRepository;
import com.solcho.bootcamp.content.repository.ContentRepository;
import com.solcho.bootcamp.progress.entity.ChapterProgress;
import com.solcho.bootcamp.progress.repository.ChapterProgressRepository;
import com.solcho.bootcamp.unit.entity.Unit;
import com.solcho.bootcamp.unit.repository.UnitRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 챕터 목록/본문 조회 + 챕터 방문 기록.
 * 조회는 비로그인 허용(공개), 방문 기록은 인증 필요(컨트롤러/시큐리티에서 처리).
 */
@Service
public class ChapterService {

    private final UnitRepository unitRepository;
    private final ContentRepository contentRepository;
    private final ContentChapterRepository chapterRepository;
    private final ChapterProgressRepository chapterProgressRepository;
    private final ActivityLogService activityLogService;

    public ChapterService(UnitRepository unitRepository,
                          ContentRepository contentRepository,
                          ContentChapterRepository chapterRepository,
                          ChapterProgressRepository chapterProgressRepository,
                          ActivityLogService activityLogService) {
        this.unitRepository = unitRepository;
        this.contentRepository = contentRepository;
        this.chapterRepository = chapterRepository;
        this.chapterProgressRepository = chapterProgressRepository;
        this.activityLogService = activityLogService;
    }

    /** 단원의 챕터 목록(본문 제외). 콘텐츠가 없으면 빈 목록. */
    @Transactional(readOnly = true)
    public List<ChapterSummaryResponse> listChapters(String unitCode) {
        Unit unit = findUnit(unitCode);
        Content content = contentRepository.findFirstByUnitIdOrderByIdAsc(unit.getId()).orElse(null);
        if (content == null) {
            return List.of();
        }
        return chapterRepository.findByContentIdOrderBySortOrderAsc(content.getId()).stream()
                .map(ChapterSummaryResponse::of)
                .toList();
    }

    /** 단원의 특정 순서 챕터 본문 + 이전/다음 챕터. */
    @Transactional(readOnly = true)
    public ChapterDetailResponse getChapter(String unitCode, int sortOrder) {
        Unit unit = findUnit(unitCode);
        Content content = contentRepository.findFirstByUnitIdOrderByIdAsc(unit.getId())
                .orElseThrow(() -> ApiException.notFound("등록된 학습 콘텐츠가 없습니다."));
        List<ContentChapter> chapters = chapterRepository.findByContentIdOrderBySortOrderAsc(content.getId());

        int idx = -1;
        for (int i = 0; i < chapters.size(); i++) {
            if (chapters.get(i).getSortOrder() == sortOrder) {
                idx = i;
                break;
            }
        }
        if (idx < 0) {
            throw ApiException.notFound("존재하지 않는 챕터입니다.");
        }
        ContentChapter current = chapters.get(idx);
        ChapterDetailResponse.NavItem prev = idx > 0 ? navOf(chapters.get(idx - 1)) : null;
        ChapterDetailResponse.NavItem next = idx < chapters.size() - 1 ? navOf(chapters.get(idx + 1)) : null;
        return ChapterDetailResponse.of(unit.getCode(), current, prev, next);
    }

    /** 챕터 방문 기록(upsert, 이미 있으면 무시). 신규 방문일 때만 잔디 기록. */
    @Transactional
    public void visit(Long userId, Long chapterId) {
        if (!chapterRepository.existsById(chapterId)) {
            throw ApiException.notFound("존재하지 않는 챕터입니다.");
        }
        if (chapterProgressRepository.existsByUserIdAndChapterId(userId, chapterId)) {
            return; // 이미 방문 → 무시
        }
        chapterProgressRepository.save(ChapterProgress.builder()
                .userId(userId)
                .chapterId(chapterId)
                .build());
        activityLogService.record(userId);
    }

    private Unit findUnit(String unitCode) {
        return unitRepository.findByCode(unitCode)
                .orElseThrow(() -> ApiException.notFound("존재하지 않는 단원입니다."));
    }

    private static ChapterDetailResponse.NavItem navOf(ContentChapter c) {
        return new ChapterDetailResponse.NavItem(c.getSortOrder(), c.getTitle());
    }
}
