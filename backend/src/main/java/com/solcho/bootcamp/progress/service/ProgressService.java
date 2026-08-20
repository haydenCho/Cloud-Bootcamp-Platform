package com.solcho.bootcamp.progress.service;

import com.solcho.bootcamp.blank.repository.BlankAnswerRepository;
import com.solcho.bootcamp.blank.repository.BlankQuestionRepository;
import com.solcho.bootcamp.common.exception.ApiException;
import com.solcho.bootcamp.progress.dto.ProgressResponse;
import com.solcho.bootcamp.progress.dto.UnitProgressSummary;
import com.solcho.bootcamp.progress.entity.Progress;
import com.solcho.bootcamp.progress.repository.ProgressRepository;
import com.solcho.bootcamp.unit.entity.Unit;
import com.solcho.bootcamp.unit.entity.UnitType;
import com.solcho.bootcamp.unit.repository.UnitRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 일반 학습 진도(progress) 저장/조회.
 * GET 요약은 대시보드용으로 progress(scroll) + blank(정답률)를 합쳐 반환한다.
 */
@Service
public class ProgressService {

    private final ProgressRepository progressRepository;
    private final UnitRepository unitRepository;
    private final BlankQuestionRepository blankQuestionRepository;
    private final BlankAnswerRepository blankAnswerRepository;

    public ProgressService(ProgressRepository progressRepository,
                           UnitRepository unitRepository,
                           BlankQuestionRepository blankQuestionRepository,
                           BlankAnswerRepository blankAnswerRepository) {
        this.progressRepository = progressRepository;
        this.unitRepository = unitRepository;
        this.blankQuestionRepository = blankQuestionRepository;
        this.blankAnswerRepository = blankAnswerRepository;
    }

    /** 스크롤 진도 upsert. scroll_percent >= 90 이면 완료 처리(엔티티에서 처리). */
    @Transactional
    public ProgressResponse updateScroll(Long userId, String code, int scrollPercent) {
        Unit unit = unitRepository.findByCode(code)
                .orElseThrow(() -> ApiException.notFound("존재하지 않는 단원입니다."));

        Progress progress = progressRepository.findByUserIdAndUnitId(userId, unit.getId())
                .orElseGet(() -> Progress.builder()
                        .userId(userId)
                        .unitId(unit.getId())
                        .scrollPercent(0)
                        .build());
        progress.applyScroll(scrollPercent);
        progressRepository.save(progress);
        return ProgressResponse.of(unit.getCode(), progress);
    }

    /**
     * 로그인 사용자의 GENERAL 단원별 진도 요약.
     * generalPercent = scroll_percent, blankPercent = 맞힌 빈칸 / 전체 빈칸.
     */
    @Transactional(readOnly = true)
    public List<UnitProgressSummary> getSummary(Long userId) {
        List<Unit> units = unitRepository.findAllByOrderBySortOrderAsc();
        List<UnitProgressSummary> result = new ArrayList<>();
        for (Unit unit : units) {
            if (unit.getType() != UnitType.GENERAL) {
                continue; // PRACTICE 는 5단계(mission_progress)에서 별도 처리
            }
            int generalPercent = progressRepository.findByUserIdAndUnitId(userId, unit.getId())
                    .map(Progress::getScrollPercent)
                    .orElse(0);

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
}
