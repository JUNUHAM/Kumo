package net.kumo.kumo.service;

import lombok.RequiredArgsConstructor;
import net.kumo.kumo.domain.dto.AdminDashboardDTO;
import net.kumo.kumo.domain.dto.ReportDTO;
import net.kumo.kumo.domain.entity.BaseEntity;
import net.kumo.kumo.domain.entity.JobPostingEntity;
import net.kumo.kumo.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final OsakaGeocodedRepository osakaGeoRepo;
    private final TokyoGeocodedRepository tokyoGeoRepo;
    private final OsakaNoGeocodedRepository osakaNoRepo;
    private final TokyoNoGeocodedRepository tokyoNoRepo;
    private final JobPostingRepository jobPostingRepository;
    private final ReportRepository reportRepository;

    @Transactional(readOnly = true)
    public List<JobPostingEntity> getAllJobPostings() {
        return jobPostingRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public List<ReportDTO> getAllReports() {
        return reportRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(ReportDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AdminDashboardDTO getDashboardData() {
        // 기준: 최근 7일 (오늘 포함)
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(6).withHour(0).withMinute(0);

        // 1. 전체 공고 수 (4개 테이블 합산)
        long totalPosts = osakaGeoRepo.count() + tokyoGeoRepo.count()
                + osakaNoRepo.count() + tokyoNoRepo.count();

        // 2. 신규 공고 수 (BaseRepository 공통 메소드 활용)
        long newPosts = osakaGeoRepo.countByCreatedAtAfter(sevenDaysAgo)
                + tokyoGeoRepo.countByCreatedAtAfter(sevenDaysAgo)
                + osakaNoRepo.countByCreatedAtAfter(sevenDaysAgo)
                + tokyoNoRepo.countByCreatedAtAfter(sevenDaysAgo);

        // 3. 주간 통계 (바 차트용)
        List<BaseEntity> recentPosts = new ArrayList<>();
        recentPosts.addAll(osakaGeoRepo.findByCreatedAtAfter(sevenDaysAgo));
        recentPosts.addAll(tokyoGeoRepo.findByCreatedAtAfter(sevenDaysAgo));
        recentPosts.addAll(osakaNoRepo.findByCreatedAtAfter(sevenDaysAgo));
        recentPosts.addAll(tokyoNoRepo.findByCreatedAtAfter(sevenDaysAgo));

        // 날짜별 그룹핑 (yyyy-MM-dd)
        Map<String, Long> weeklyStats = recentPosts.stream()
                .collect(Collectors.groupingBy(
                        post -> post.getCreatedAt().toLocalDate().format(DateTimeFormatter.ISO_DATE),
                        Collectors.counting()
                ));
        weeklyStats = fillMissingDates(weeklyStats, 7); // 빈 날짜 0으로 채우기

        // 4. 지역별 통계 (도넛 차트용 - 좌표 있는 리포지토리만)
        Map<String, Long> osakaWards = listToMap(osakaGeoRepo.countByWard());
        Map<String, Long> tokyoWards = listToMap(tokyoGeoRepo.countByWard());

        // 5. 회원 통계 (임시 데이터)
        Map<String, Long> mockUserStats = new LinkedHashMap<>();
        mockUserStats.put("Jan", 120L); mockUserStats.put("Feb", 150L);

        return AdminDashboardDTO.builder()
                .totalUsers(750000L) // 임시
                .newUsers(7500L)     // 임시
                .totalPosts(totalPosts)
                .newPosts(newPosts)
                .weeklyPostStats(weeklyStats)
                .osakaWardStats(osakaWards)
                .tokyoWardStats(tokyoWards)
                .monthlyUserStats(mockUserStats)
                .build();
    }

    // List<Object[]> -> Map 변환 헬퍼
    private Map<String, Long> listToMap(List<Object[]> list) {
        Map<String, Long> map = new HashMap<>();
        for (Object[] row : list) {
            String key = (String) row[0];
            Long val = (Long) row[1];
            if (key != null) map.put(key, val);
        }
        return map;
    }

    // 날짜 채우기 헬퍼
    private Map<String, Long> fillMissingDates(Map<String, Long> data, int days) {
        Map<String, Long> sorted = new TreeMap<>(data);
        LocalDate today = LocalDate.now();
        for (int i = 0; i < days; i++) {
            String date = today.minusDays(i).format(DateTimeFormatter.ISO_DATE);
            sorted.putIfAbsent(date, 0L);
        }
        return sorted;
    }
}