package net.kumo.kumo.service;

import lombok.RequiredArgsConstructor;
import net.kumo.kumo.domain.dto.ApplicationRequestDTO;
import net.kumo.kumo.domain.dto.JobDetailDTO;
import net.kumo.kumo.domain.dto.JobSummaryDTO;
import net.kumo.kumo.domain.dto.ReportDTO;
import net.kumo.kumo.domain.dto.projection.JobSummaryView;
import net.kumo.kumo.domain.entity.ApplicationEntity;
import net.kumo.kumo.domain.entity.BaseEntity;
import net.kumo.kumo.domain.entity.ReportEntity;
import net.kumo.kumo.domain.entity.UserEntity;
import net.kumo.kumo.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MapService {

    private final OsakaGeocodedRepository osakaRepo;
    private final TokyoGeocodedRepository tokyoRepo;
    private final OsakaNoGeocodedRepository osakaNoRepo;
    private final TokyoNoGeocodedRepository tokyoNoRepo;

    // 신고 관련 리포지토리
    private final ReportRepository reportRepo;
    private final UserRepository userRepo; // ★ [추가] 신고자(User) 조회를 위해 필요

    // 공고 신청 리포지토리
    private final ApplicationRepository applicationRepo;

    // --- 1. 지도용 리스트 조회 ---
    @Transactional(readOnly = true)
    public List<JobSummaryDTO> getJobListInMap(Double minLat, Double maxLat, Double minLng, Double maxLng, String lang) {
        List<JobSummaryView> osakaRaw = osakaRepo.findTop300ByLatBetweenAndLngBetween(minLat, maxLat, minLng, maxLng);
        List<JobSummaryDTO> result = new ArrayList<>(osakaRaw.stream()
                .map(view -> new JobSummaryDTO(view, lang, "OSAKA"))
                .toList());

        List<JobSummaryView> tokyoRaw = tokyoRepo.findTop300ByLatBetweenAndLngBetween(minLat, maxLat, minLng, maxLng);
        result.addAll(tokyoRaw.stream()
                .map(view -> new JobSummaryDTO(view, lang, "TOKYO"))
                .toList());

        return result;
    }

    // --- 2. 상세 페이지 조회 ---
    @Transactional(readOnly = true)
    public JobDetailDTO getJobDetail(Long id, String source, String lang) {
        BaseEntity entity = null;

        // 소스에 따라 적절한 리포지토리 선택
        if ("OSAKA".equalsIgnoreCase(source)) {
            entity = osakaRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 공고입니다."));
        } else if ("TOKYO".equalsIgnoreCase(source)) {
            entity = tokyoRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 공고입니다."));
        } else if ("OSAKA_NO".equalsIgnoreCase(source)) {
            entity = osakaNoRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 공고입니다."));
        } else if ("TOKYO_NO".equalsIgnoreCase(source)) {
            entity = tokyoNoRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 공고입니다."));
        } else {
            throw new IllegalArgumentException("잘못된 접근입니다 (Source 오류).");
        }

        // JobDetailDTO 생성자에 source도 함께 전달
        return new JobDetailDTO(entity, lang, source);
    }

    // --- 3. [수정] 신고 등록 ---
    @Transactional
    public void createReport(ReportDTO dto) {
        // 1. 신고자(User) 조회
        // DTO에 있는 reporterId로 실제 유저 엔티티를 찾아야 연관관계를 맺을 수 있음
        UserEntity reporter = userRepo.findById(dto.getReporterId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 2. 엔티티 변환 및 저장 (변경된 DB 구조 반영)
        ReportEntity report = ReportEntity.builder()
                .reporter(reporter)            // [변경] ID 대신 UserEntity 객체 주입
                .targetPostId(dto.getTargetPostId())
                .targetSource(dto.getTargetSource()) // [변경] 이제 별도 컬럼에 저장
                .reasonCategory(dto.getReasonCategory())
                .description(dto.getDescription())   // [변경] 순수 본문만 저장 (앞에 [OSAKA] 안 붙임)
                .status("PENDING")             // 기본 상태
                .build();

        reportRepo.save(report);
    }

    // --- 4. 구인 신청(지원하기) 로직 ---
    @Transactional
    public void applyForJob(UserEntity seeker, ApplicationRequestDTO dto) {

        // 1. 중복 지원 검사 (DB 보호 및 프론트엔드 알림용)
        boolean alreadyApplied = applicationRepo.existsByTargetSourceAndTargetPostIdAndSeeker(
                dto.getTargetSource(),
                dto.getTargetPostId(),
                seeker
        );

        if (alreadyApplied) {
            // 이미 지원한 경우, 컨트롤러의 catch 블록으로 에러 메시지를 던짐
            throw new IllegalStateException("이미 지원하신 공고입니다.");
        }

        // 2. 지원 내역 엔티티 생성
        ApplicationEntity application = ApplicationEntity.builder()
                .targetSource(dto.getTargetSource())
                .targetPostId(dto.getTargetPostId())
                .seeker(seeker)
                // status는 엔티티의 @Builder.Default 설정에 의해 'APPLIED'로 자동 들어갑니다.
                .build();

        // 3. DB 저장
        applicationRepo.save(application);
    }
}