package com.solcho.bootcamp.unit.service;

import com.solcho.bootcamp.blank.repository.BlankQuestionRepository;
import com.solcho.bootcamp.content.repository.ContentRepository;
import com.solcho.bootcamp.mission.repository.PracticeMissionRepository;
import com.solcho.bootcamp.unit.dto.UnitResponse;
import com.solcho.bootcamp.unit.entity.Unit;
import com.solcho.bootcamp.unit.repository.UnitRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UnitService {

    private final UnitRepository unitRepository;
    private final ContentRepository contentRepository;
    private final BlankQuestionRepository blankQuestionRepository;
    private final PracticeMissionRepository missionRepository;

    public UnitService(UnitRepository unitRepository,
                       ContentRepository contentRepository,
                       BlankQuestionRepository blankQuestionRepository,
                       PracticeMissionRepository missionRepository) {
        this.unitRepository = unitRepository;
        this.contentRepository = contentRepository;
        this.blankQuestionRepository = blankQuestionRepository;
        this.missionRepository = missionRepository;
    }

    /**
     * sort_order 순 전체 단원 목록.
     * 각 단원의 콘텐츠 유무 / 빈칸 수 / 미션 수를 그룹 집계(각 1쿼리)로 함께 채운다(/study 카드용).
     */
    @Transactional(readOnly = true)
    public List<UnitResponse> getAllUnits() {
        Set<Long> unitsWithContent = Set.copyOf(contentRepository.findUnitIdsWithContent());
        Map<Long, Integer> blankCounts = toCountMap(blankQuestionRepository.countGroupedByUnit());
        Map<Long, Integer> missionCounts = toCountMap(missionRepository.countGroupedByUnit());

        return unitRepository.findAllByOrderBySortOrderAsc().stream()
                .map(u -> UnitResponse.of(
                        u,
                        unitsWithContent.contains(u.getId()),
                        blankCounts.getOrDefault(u.getId(), 0),
                        missionCounts.getOrDefault(u.getId(), 0)))
                .toList();
    }

    private Map<Long, Integer> toCountMap(List<Object[]> rows) {
        Map<Long, Integer> map = new HashMap<>();
        for (Object[] row : rows) {
            map.put((Long) row[0], ((Long) row[1]).intValue());
        }
        return map;
    }
}
