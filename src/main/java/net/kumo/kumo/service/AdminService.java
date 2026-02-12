package net.kumo.kumo.service;

import lombok.RequiredArgsConstructor;
import net.kumo.kumo.domain.dto.AdminDashboardDTO;
import net.kumo.kumo.domain.dto.ReportDTO;
import net.kumo.kumo.domain.entity.BaseEntity;
import net.kumo.kumo.domain.entity.JobPostingEntity;
import net.kumo.kumo.domain.entity.ReportEntity;
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
    private final JobPostingRepository jobPostingRepo;
    private final ReportRepository reportRepo;
    private final UserRepository userRepo;

    @Transactional(readOnly = true)
    public List<JobPostingEntity> getAllJobPostings() {
        return jobPostingRepo.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public List<ReportDTO> getAllReports() {
        List<ReportEntity> entities = reportRepo.findAllByOrderByCreatedAtDesc();

        return entities.stream().map(entity -> {
            ReportDTO dto = ReportDTO.fromEntity(entity);
            // 2. 공고 제목 찾기 (targetPostId 이용)
            // (만약 외부 크롤링 데이터라면 제목 찾기가 어려울 수 있으니 예외처리)
            if (entity.getTargetPostId() != null) {
                jobPostingRepo.findById(entity.getTargetPostId())
                        .ifPresentOrElse(
                                post -> dto.setTargetPostTitle(post.getTitle()), // 찾으면 제목 설정
                                () -> dto.setTargetPostTitle("삭제된 공고 또는 외부 공고 (ID: " + entity.getTargetPostId() + ")") // 없으면 ID 표시
                        );
            } else {
                dto.setTargetPostTitle("정보 없음");
            }

            if (entity.getReporterId() != null) {
                userRepo.findById(entity.getReporterId())
                        .ifPresentOrElse(
                                user -> dto.setReporterEmail(user.getEmail()),
                                () -> dto.setReporterEmail("알 수 없음 (ID: " + entity.getReporterId() + ")")
                        );
            } else {
                dto.setReporterEmail("비회원/익명");
            }

            return dto;
        }).collect(Collectors.toList());
    }

    // ★ [추가] 공고 일괄 삭제
    @Transactional
    public void deleteJobPostings(List<Long> ids) {
        // null 체크나 빈 리스트 체크를 해도 좋지만, JPA가 알아서 처리해주기도 함
        if (ids != null && !ids.isEmpty()) {
            jobPostingRepo.deleteAllById(ids);
        }
    }

    // ★ [추가] 신고 내역 일괄 삭제
    @Transactional
    public void deleteReports(List<Long> ids) {
        if (ids != null && !ids.isEmpty()) {
            reportRepo.deleteAllById(ids);
        }
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