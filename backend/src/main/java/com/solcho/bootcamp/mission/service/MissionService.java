package com.solcho.bootcamp.mission.service;

import com.solcho.bootcamp.activity.service.ActivityLogService;
import com.solcho.bootcamp.common.exception.ApiException;
import com.solcho.bootcamp.mission.dto.MissionListResponse;
import com.solcho.bootcamp.mission.dto.MissionResponse;
import com.solcho.bootcamp.mission.dto.VerifyResponse;
import com.solcho.bootcamp.mission.entity.MissionProgress;
import com.solcho.bootcamp.mission.entity.PracticeMission;
import com.solcho.bootcamp.mission.repository.MissionProgressRepository;
import com.solcho.bootcamp.mission.repository.PracticeMissionRepository;
import com.solcho.bootcamp.mission.verify.MissionVerifierRegistry;
import com.solcho.bootcamp.unit.entity.Unit;
import com.solcho.bootcamp.unit.repository.UnitRepository;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MissionService {

    private final PracticeMissionRepository missionRepository;
    private final MissionProgressRepository progressRepository;
    private final UnitRepository unitRepository;
    private final MissionVerifierRegistry verifierRegistry;
    private final ActivityLogService activityLogService;

    public MissionService(PracticeMissionRepository missionRepository,
                          MissionProgressRepository progressRepository,
                          UnitRepository unitRepository,
                          MissionVerifierRegistry verifierRegistry,
                          ActivityLogService activityLogService) {
        this.missionRepository = missionRepository;
        this.progressRepository = progressRepository;
        this.unitRepository = unitRepository;
        this.verifierRegistry = verifierRegistry;
        this.activityLogService = activityLogService;
    }

    /**
     * 단원의 미션 목록 + XP 요약. userId 가 있으면 완료 여부/획득 XP 를 채운다.
     */
    @Transactional(readOnly = true)
    public MissionListResponse getMissions(String code, Long userId) {
        Unit unit = unitRepository.findByCode(code)
                .orElseThrow(() -> ApiException.notFound("존재하지 않는 단원입니다."));
        List<PracticeMission> missions = missionRepository.findByUnitIdOrderBySortOrderAsc(unit.getId());

        final Set<Long> completedIds;
        if (userId != null && !missions.isEmpty()) {
            List<Long> ids = missions.stream().map(PracticeMission::getId).toList();
            completedIds = progressRepository.findByUserIdAndMissionIdIn(userId, ids).stream()
                    .filter(MissionProgress::isCompleted)
                    .map(MissionProgress::getMissionId)
                    .collect(Collectors.toSet());
        } else {
            completedIds = Set.of();
        }

        int totalXp = 0;
        int earnedXp = 0;
        List<MissionResponse> items = missions.stream().map(m -> {
            boolean completed = completedIds.contains(m.getId());
            return new MissionResponse(m.getId(), m.getTitle(), m.getDescription(),
                    m.getMissionType(), m.getXpReward(), m.getSortOrder(), completed);
        }).toList();

        for (PracticeMission m : missions) {
            totalXp += m.getXpReward();
            if (completedIds.contains(m.getId())) {
                earnedXp += m.getXpReward();
            }
        }
        return new MissionListResponse(items, earnedXp, totalXp);
    }

    /**
     * 미션 정답 검증 + (성공 시) 완료 upsert.
     * mission_type 에 맞는 Verifier 로 분기하며, 어떤 경우에도 사용자 입력을 실행하지 않는다.
     */
    @Transactional
    public VerifyResponse verify(Long userId, Long missionId, String input) {
        PracticeMission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> ApiException.notFound("존재하지 않는 미션입니다."));

        boolean correct = verifierRegistry.get(mission.getMissionType())
                .verify(mission.getVerifyPattern(), input);

        boolean completed;
        if (correct) {
            var existing = progressRepository.findByUserIdAndMissionId(userId, missionId);
            boolean alreadyCompleted = existing.map(MissionProgress::isCompleted).orElse(false);
            MissionProgress progress = existing
                    .orElseGet(() -> progressRepository.save(MissionProgress.builder()
                            .userId(userId)
                            .missionId(missionId)
                            .build()));
            progress.markCompleted();
            completed = true;
            // 처음 완료한 미션만 잔디 기록(이미 완료한 미션 재검증은 제외)
            if (!alreadyCompleted) {
                activityLogService.record(userId);
            }
        } else {
            // 이미 완료된 미션이면 오답을 내도 완료 상태는 유지
            completed = progressRepository.findByUserIdAndMissionId(userId, missionId)
                    .map(MissionProgress::isCompleted)
                    .orElse(false);
        }

        return new VerifyResponse(correct, completed, mission.getXpReward());
    }
}
