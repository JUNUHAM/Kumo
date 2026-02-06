package net.kumo.kumo.service;

import net.kumo.kumo.domain.entity.BaseEntity;
import net.kumo.kumo.repository.OsakaGeocodedRepository;
import net.kumo.kumo.repository.TokyoGeocodedRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import net.kumo.kumo.domain.dto.JobSummaryDTO;
import net.kumo.kumo.domain.dto.JobDetailDTO;
import net.kumo.kumo.domain.dto.projection.JobSummaryView;
import net.kumo.kumo.repository.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MapService {

    private final OsakaGeocodedRepository osakaRepo;
    private final TokyoGeocodedRepository tokyoRepo;
    private final OsakaNoGeocodedRepository osakaNoRepo;
    private final TokyoNoGeocodedRepository tokyoNoRepo;

    // ★ 반환 타입 변경: JobSummaryView -> JobSummaryDTO
    // ★ 파라미터 추가: String lang
    @Transactional(readOnly = true)
    public List<JobSummaryDTO> getJobListInMap(Double minLat, Double maxLat, Double minLng, Double maxLng, String lang) {

        // 오사카 리포에서 데이터 꺼냄
        List<JobSummaryView> osakaRaw = osakaRepo.findTop300ByLatBetweenAndLngBetween(minLat, maxLat, minLng, maxLng);
        // 오사카 source 추가
        List<JobSummaryDTO> result = new ArrayList<>(osakaRaw.stream()
                .map(view -> new JobSummaryDTO(view, lang, "OSAKA"))
                .toList());

        // 도쿄 리포에서 데이터 꺼냄
        List<JobSummaryView> tokyoRaw = tokyoRepo.findTop300ByLatBetweenAndLngBetween(minLat, maxLat, minLng, maxLng);
        // 도쿄 source 추가
        result.addAll(tokyoRaw.stream()
                .map(view -> new JobSummaryDTO(view, lang, "TOKYO"))
                .toList());

        return result;
    }

    /**
     * 공고 상세 정보를 조회
     * * @param id     공고 ID (PK)
     * @param source 데이터 출처 (허용 값: "OSAKA", "OSAKA_NO", "TOKYO", "TOKYO_NO")
     * @param lang   언어 설정 ("kr" 또는 "jp")
     * @return       상세 정보 DTO
     * @throws IllegalArgumentException 해당 공고가 없거나 source 값이 잘못된 경우
     */
    @Transactional(readOnly = true)
    public JobDetailDTO getJobDetail(Long id, String source, String lang) {
        BaseEntity entity = null;

        // source 파라미터를 보고 어느 테이블에서 가져올지 결정
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

        // 엔티티를 DTO로 변환해서 반환
        return new JobDetailDTO(entity, lang);
    }
}