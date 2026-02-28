package net.kumo.kumo.domain.dto;

import lombok.Builder;
import lombok.Getter;
import java.util.Map;

@Getter
@Builder
public class AdminDashboardDTO {
    // --- 1. 상단 통계 카드 ---
    private long totalUsers;    // 전체 회원 (임시)
    private long newUsers;      // 신규 회원 (임시)
    private long totalPosts;    // 전체 공고 (실제 DB)
    private long newPosts;      // 신규 공고 (7일 이내, 실제 DB)

    // --- 2. 차트 데이터 ---
    // 주간 공고 등록 수 (바 차트) -> 날짜 문자열(yyyy-MM-dd) : 개수
    private Map<String, Long> weeklyPostStats;

    // 지역별 공고 수 (도넛 차트) -> 구 이름 : 개수
    private Map<String, Long> osakaWardStats;
    private Map<String, Long> tokyoWardStats;

    // 월별 신규 회원 수 (라인 차트) -> 월 이름 : 개수 (임시)
    private Map<String, Long> monthlyUserStats;
}