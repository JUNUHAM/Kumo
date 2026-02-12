package net.kumo.kumo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.kumo.kumo.domain.dto.AdminDashboardDTO;
import net.kumo.kumo.domain.dto.JobSummaryDTO;
import net.kumo.kumo.domain.dto.ReportDTO;
import net.kumo.kumo.domain.entity.*;
import net.kumo.kumo.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {

    // 크롤링 데이터 리포지토리 (4종)
    private final OsakaGeocodedRepository osakaGeoRepo;
    private final TokyoGeocodedRepository tokyoGeoRepo;
    private final OsakaNoGeocodedRepository osakaNoRepo;
    private final TokyoNoGeocodedRepository tokyoNoRepo;

    // 신고/유저 리포지토리
    private final ReportRepository reportRepo;
    private final UserRepository userRepo;

    // =================================================================
    // 1. 전체 공고 통합 조회 (Lang 적용)
    // =================================================================
    @Transactional(readOnly = true)
    public List<JobSummaryDTO> getAllJobSummaries(String lang) {
        List<JobSummaryDTO> unifiedList = new ArrayList<>();

        // 1. Osaka Geocoded
        unifiedList.addAll(osakaGeoRepo.findAll().stream()
                .map(e -> new JobSummaryDTO(e, lang, "OSAKA"))
                .toList());

        // 2. Tokyo Geocoded
        unifiedList.addAll(tokyoGeoRepo.findAll().stream()
                .map(e -> new JobSummaryDTO(e, lang, "TOKYO"))
                .toList());

        // 3. Osaka No-Geocoded
        unifiedList.addAll(osakaNoRepo.findAll().stream()
                .map(e -> new JobSummaryDTO(e, lang, "OSAKA_NO"))
                .toList());

        // 4. Tokyo No-Geocoded
        unifiedList.addAll(tokyoNoRepo.findAll().stream()
                .map(e -> new JobSummaryDTO(e, lang, "TOKYO_NO"))
                .toList());

        // 5. 최신순 정렬
        unifiedList.sort((a, b) -> {
            String timeA = a.getWriteTime();
            String timeB = b.getWriteTime();
            if (timeB == null) return -1;
            if (timeA == null) return 1;
            return timeB.compareTo(timeA);
        });

        return unifiedList;
    }

    // =================================================================
    // 2. 신고 목록 조회 (Lang 적용 - 제목 번역)
    // =================================================================
    @Transactional(readOnly = true)
    public List<ReportDTO> getAllReports(String lang) { // ★ lang 파라미터 추가
        List<ReportEntity> entities = reportRepo.findAllByOrderByCreatedAtDesc();
        boolean isJp = "ja".equalsIgnoreCase(lang); // 언어 체크

        return entities.stream().map(entity -> {
            ReportDTO dto = ReportDTO.fromEntity(entity);

            // 신고자 이메일
            if (entity.getReporter() != null) {
                dto.setReporterEmail(entity.getReporter().getEmail());
            } else {
                dto.setReporterEmail(isJp ? "不明" : "알 수 없음");
            }

            // 공고 제목 찾기
            String source = entity.getTargetSource();
            Long targetId = entity.getTargetPostId();

            // 기본 메시지 다국어 처리
            String title = isJp ? "削除された求人" : "삭제된 공고";
            String deletedSuffix = isJp ? "(削除済み)" : "(삭제됨)";

            try {
                BaseEntity targetEntity = null;

                // 리포지토리에서 엔티티 조회
                if ("OSAKA".equals(source)) {
                    targetEntity = osakaGeoRepo.findById(targetId).orElse(null);
                } else if ("TOKYO".equals(source)) {
                    targetEntity = tokyoGeoRepo.findById(targetId).orElse(null);
                } else if ("OSAKA_NO".equals(source)) {
                    targetEntity = osakaNoRepo.findById(targetId).orElse(null);
                } else if ("TOKYO_NO".equals(source)) {
                    targetEntity = tokyoNoRepo.findById(targetId).orElse(null);
                }

                // 엔티티가 존재하면 언어에 맞는 제목 추출
                if (targetEntity != null) {
                    if (isJp && hasText(targetEntity.getTitleJp())) {
                        title = targetEntity.getTitleJp();
                    } else {
                        title = targetEntity.getTitle();
                    }
                } else {
                    title = title + " " + source; // 삭제된 경우
                }

            } catch (Exception e) {
                log.warn("신고 대상 공고 조회 실패: ID={}, Source={}", targetId, source);
            }

            dto.setTargetPostTitle(title);
            return dto;
        }).collect(Collectors.toList());
    }

    // =================================================================
    // 3. 공고 일괄 삭제
    // =================================================================
    @Transactional
    public void deleteMixedPosts(List<String> mixedIds) {
        if (mixedIds == null || mixedIds.isEmpty()) return;

        for (String mixedId : mixedIds) {
            try {
                int lastUnderscore = mixedId.lastIndexOf('_');
                if (lastUnderscore == -1) continue;

                String source = mixedId.substring(0, lastUnderscore);
                Long id = Long.parseLong(mixedId.substring(lastUnderscore + 1));

                switch (source) {
                    case "OSAKA" -> osakaGeoRepo.deleteById(id);
                    case "TOKYO" -> tokyoGeoRepo.deleteById(id);
                    case "OSAKA_NO" -> osakaNoRepo.deleteById(id);
                    case "TOKYO_NO" -> tokyoNoRepo.deleteById(id);
                    default -> log.warn("알 수 없는 Source: {}", source);
                }
            } catch (Exception e) {
                log.error("삭제 처리 중 오류 발생: {}", mixedId, e);
            }
        }
    }

    // =================================================================
    // 4. 신고 내역 삭제
    // =================================================================
    @Transactional
    public void deleteReports(List<Long> ids) {
        if (ids != null && !ids.isEmpty()) {
            reportRepo.deleteAllById(ids);
        }
    }

    // =================================================================
    // 5. 대시보드 데이터
    // =================================================================
    @Transactional(readOnly = true)
    public AdminDashboardDTO getDashboardData() {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(6).withHour(0).withMinute(0);

        long totalPosts = osakaGeoRepo.count() + tokyoGeoRepo.count()
                + osakaNoRepo.count() + tokyoNoRepo.count();

        long newPosts = osakaGeoRepo.countByCreatedAtAfter(sevenDaysAgo)
                + tokyoGeoRepo.countByCreatedAtAfter(sevenDaysAgo)
                + osakaNoRepo.countByCreatedAtAfter(sevenDaysAgo)
                + tokyoNoRepo.countByCreatedAtAfter(sevenDaysAgo);

        List<BaseEntity> recentPosts = new ArrayList<>();
        recentPosts.addAll(osakaGeoRepo.findByCreatedAtAfter(sevenDaysAgo));
        recentPosts.addAll(tokyoGeoRepo.findByCreatedAtAfter(sevenDaysAgo));
        recentPosts.addAll(osakaNoRepo.findByCreatedAtAfter(sevenDaysAgo));
        recentPosts.addAll(tokyoNoRepo.findByCreatedAtAfter(sevenDaysAgo));

        Map<String, Long> weeklyStats = recentPosts.stream()
                .collect(Collectors.groupingBy(
                        post -> post.getCreatedAt().toLocalDate().format(DateTimeFormatter.ISO_DATE),
                        Collectors.counting()
                ));
        weeklyStats = fillMissingDates(weeklyStats, 7);

        Map<String, Long> osakaWards = listToMap(osakaGeoRepo.countByWard());
        Map<String, Long> tokyoWards = listToMap(tokyoGeoRepo.countByWard());

        Map<String, Long> mockUserStats = new LinkedHashMap<>();
        mockUserStats.put("Jan", 120L); mockUserStats.put("Feb", 150L);

        return AdminDashboardDTO.builder()
                .totalUsers(userRepo.count())
                .newUsers(0L)
                .totalPosts(totalPosts)
                .newPosts(newPosts)
                .weeklyPostStats(weeklyStats)
                .osakaWardStats(osakaWards)
                .tokyoWardStats(tokyoWards)
                .monthlyUserStats(mockUserStats)
                .build();
    }

    // --- Helper Methods ---
    private Map<String, Long> listToMap(List<Object[]> list) {
        Map<String, Long> map = new HashMap<>();
        for (Object[] row : list) {
            String key = (String) row[0];
            Long val = (Long) row[1];
            if (key != null) map.put(key, val);
        }
        return map;
    }

    private Map<String, Long> fillMissingDates(Map<String, Long> data, int days) {
        Map<String, Long> sorted = new TreeMap<>(data);
        LocalDate today = LocalDate.now();
        for (int i = 0; i < days; i++) {
            String date = today.minusDays(i).format(DateTimeFormatter.ISO_DATE);
            sorted.putIfAbsent(date, 0L);
        }
        return sorted;
    }

    private boolean hasText(String str) {
        return str != null && !str.isBlank();
    }
}