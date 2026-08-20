package com.solcho.bootcamp.activity.service;

import com.solcho.bootcamp.activity.dto.ActivityDayResponse;
import com.solcho.bootcamp.activity.entity.ActivityLog;
import com.solcho.bootcamp.activity.repository.ActivityLogRepository;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 잔디심기 활동 기록/조회.
 * record()는 "학습에 실질적 진전이 있는 시점"(진도 전진 / 빈칸 신규 정답 / 미션 신규 완료)에만
 * 호출된다. 호출부의 트랜잭션에 참여한다(전파 기본값 REQUIRED).
 */
@Service
public class ActivityLogService {

    private static final int WEEKS = 26; // 약 6개월

    private final ActivityLogRepository repository;

    public ActivityLogService(ActivityLogRepository repository) {
        this.repository = repository;
    }

    /** 오늘 활동 1회 기록(없으면 생성, 있으면 카운트 증가). */
    @Transactional
    public void record(Long userId) {
        LocalDate today = LocalDate.now();
        repository.findByUserIdAndActivityDate(userId, today)
                .ifPresentOrElse(
                        ActivityLog::increment,
                        () -> repository.save(ActivityLog.builder()
                                .userId(userId)
                                .activityDate(today)
                                .build()));
    }

    /** 최근 6개월치 날짜별 { date, level, count } (일요일 정렬 시작 ~ 오늘). */
    @Transactional(readOnly = true)
    public List<ActivityDayResponse> getRecentActivity(Long userId) {
        LocalDate today = LocalDate.now();
        // 이번 주 일요일 (JS getDay() 기준과 동일: 일요일=0)
        LocalDate lastSunday = today.minusDays(today.getDayOfWeek().getValue() % 7);
        LocalDate start = lastSunday.minusWeeks(WEEKS - 1);

        Map<LocalDate, Integer> counts = new HashMap<>();
        for (ActivityLog log : repository.findByUserIdAndActivityDateGreaterThanEqual(userId, start)) {
            counts.put(log.getActivityDate(), log.getActivityCount());
        }

        List<ActivityDayResponse> result = new ArrayList<>();
        for (LocalDate d = start; !d.isAfter(today); d = d.plusDays(1)) {
            int count = counts.getOrDefault(d, 0);
            result.add(new ActivityDayResponse(d.toString(), level(count), count));
        }
        return result;
    }

    /** count 구간 → level(0~4). 0 / 1~2 / 3~4 / 5~7 / 8+ */
    private int level(int count) {
        if (count <= 0) return 0;
        if (count <= 2) return 1;
        if (count <= 4) return 2;
        if (count <= 7) return 3;
        return 4;
    }
}
