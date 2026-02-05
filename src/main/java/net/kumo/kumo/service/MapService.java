package net.kumo.kumo.service;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import net.kumo.kumo.domain.dto.JobSummaryDTO;
import net.kumo.kumo.domain.dto.projection.JobSummaryView;
import net.kumo.kumo.repository.OsakaGeocodedRepository;
import net.kumo.kumo.repository.TokyoGeocodedRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MapService {

    private final OsakaGeocodedRepository osakaRepo;
    private final TokyoGeocodedRepository tokyoRepo;

    // ★ 반환 타입 변경: JobSummaryView -> JobResponse
    // ★ 파라미터 추가: String lang
    @Transactional(readOnly = true)
    public List<JobSummaryDTO> getJobListInMap(Double minLat, Double maxLat, Double minLng, Double maxLng, String lang) {

        // 1. DB에서 데이터 조회
        List<JobSummaryView> osakaRaw = osakaRepo.findTop300ByLatBetweenAndLngBetween(minLat, maxLat, minLng, maxLng);
        List<JobSummaryView> tokyoRaw = tokyoRepo.findTop300ByLatBetweenAndLngBetween(minLat, maxLat, minLng, maxLng);

        // 2. 리스트 합치기
        List<JobSummaryView> allRaw = new ArrayList<>();
        allRaw.addAll(osakaRaw);
        allRaw.addAll(tokyoRaw);

        // 3. ★ 핵심: View -> Response DTO로 변환 (여기서 언어 필터링 발생)
        return allRaw.stream()
                .map(view -> new JobSummaryDTO(view, lang))
                .collect(Collectors.toList());
    }
}