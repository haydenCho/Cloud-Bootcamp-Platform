package com.solcho.bootcamp.progress.service;

import com.solcho.bootcamp.blank.repository.BlankAnswerRepository;
import com.solcho.bootcamp.blank.repository.BlankQuestionRepository;
import com.solcho.bootcamp.content.entity.Content;
import com.solcho.bootcamp.content.repository.ContentChapterRepository;
import com.solcho.bootcamp.content.repository.ContentRepository;
import com.solcho.bootcamp.progress.dto.UnitProgressSummary;
import com.solcho.bootcamp.progress.repository.ChapterProgressRepository;
import com.solcho.bootcamp.unit.entity.Unit;
import com.solcho.bootcamp.unit.entity.UnitType;
import com.solcho.bootcamp.unit.repository.UnitRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 대시보드 학습 진도 요약(조회 전용).
 * 8단계 개선: 스크롤 기반 progress 를 제거하고, generalPercent 를
 * "방문한 챕터 수 / 전체 챕터 수 × 100" 으로 계산한다(저장된 퍼센트 없음).
 * blankPercent 는 그대로 맞힌 빈칸 비율.
 */
@Service
public class ProgressService {

    private final UnitRepository unitRepository;
    private final ContentRepository contentRepository;
    private final ContentChapterRepository chapterRepository;
    private final ChapterProgressRepository chapterProgressRepository;
    private final BlankQuestionRepository blankQuestionRepository;
    private final BlankAnswerRepository blankAnswerRepository;

    public ProgressService(UnitRepository unitRepository,
                           ContentRepository contentRepository,
                           ContentChapterRepository chapterRepository,
                           ChapterProgressRepository chapterProgressRepository,
                           BlankQuestionRepository blankQuestionRepository,
                           BlankAnswerRepository blankAnswerRepository) {
        this.unitRepository = unitRepository;
        this.contentRepository = contentRepository;
        this.chapterRepository = chapterRepository;
        this.chapterProgressRepository = chapterProgressRepository;
        this.blankQuestionRepository = blankQuestionRepository;
        this.blankAnswerRepository = blankAnswerRepository;
    }

    /**
     * 로그인 사용자의 GENERAL 단원별 진도 요약.
     * generalPercent = 방문 챕터 / 전체 챕터, blankPercent = 맞힌 빈칸 / 전체 빈칸.
     */
    @Transactional(readOnly = true)
    public List<UnitProgressSummary> getSummary(Long userId) {
        List<Unit> units = unitRepository.findAllByOrderBySortOrderAsc();
        List<UnitProgressSummary> result = new ArrayList<>();
        for (Unit unit : units) {
            if (unit.getType() != UnitType.GENERAL) {
                continue; // PRACTICE 는 mission_progress 로 별도 처리
            }
            int generalPercent = chapterPercent(userId, unit);

            long total = blankQuestionRepository.countByUnitId(unit.getId());
            int blankPercent = 0;
            if (total > 0) {
                long correct = blankAnswerRepository.countCorrectByUserAndUnit(userId, unit.getId());
                blankPercent = (int) Math.round((correct * 100.0) / total);
            }
            result.add(new UnitProgressSummary(unit.getCode(), "GENERAL", generalPercent, blankPercent));
        }
        return result;
    }

    /** 방문한 챕터 수 / 전체 챕터 수 × 100 (콘텐츠/챕터가 없으면 0). */
    private int chapterPercent(Long userId, Unit unit) {
        Content content = contentRepository.findFirstByUnitIdOrderByIdAsc(unit.getId()).orElse(null);
        if (content == null) {
            return 0;
        }
        long total = chapterRepository.countByContentId(content.getId());
        if (total <= 0) {
            return 0;
        }
        long visited = chapterProgressRepository.countVisitedByUserAndContent(userId, content.getId());
        return (int) Math.round((visited * 100.0) / total);
    }
}
