package net.kumo.kumo.service;

import lombok.RequiredArgsConstructor;
import net.kumo.kumo.domain.dto.JobDetailDTO;
import net.kumo.kumo.domain.dto.JobSummaryDTO;
import net.kumo.kumo.domain.dto.ReportDTO;
import net.kumo.kumo.domain.dto.projection.JobSummaryView;
import net.kumo.kumo.domain.entity.BaseEntity;
import net.kumo.kumo.domain.entity.ReportEntity;
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

    // ★ [추가] 신고 저장을 위한 리포지토리
    private final ReportRepository reportRepository;

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

        // ★ [수정] DTO 생성 시 source도 함께 전달 (JobDetailDTO 생성자 수정 필요)
        return new JobDetailDTO(entity, lang, source);
    }

    // --- 3. [NEW] 신고 등록 ---
    @Transactional
    public void createReport(ReportDTO dto) {
        // 엔티티 변환 및 저장
        ReportEntity report = ReportEntity.builder()
                .reporterId(dto.getReporterId())
                .targetPostId(dto.getTargetPostId())
                // DB에 target_source 컬럼이 있다면 추가 (없다면 description에 포함)
                .description("[" + dto.getTargetSource() + "] " + dto.getDescription())
                .reasonCategory(dto.getReasonCategory())
                .status("PENDING")
                .build();

        reportRepository.save(report);
    }
}