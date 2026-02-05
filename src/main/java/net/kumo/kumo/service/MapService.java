package net.kumo.kumo.service;

import lombok.RequiredArgsConstructor;
import net.kumo.kumo.domain.dto.projection.JobSummaryView;
import net.kumo.kumo.repository.OsakaGeocodedRepository;
import net.kumo.kumo.repository.TokyoGeocodedRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MapService {

    private final OsakaGeocodedRepository osakaRepo;
    private final TokyoGeocodedRepository tokyoRepo;

    @Transactional(readOnly = true)
    public List<JobSummaryView> getJobListInMap(Double minLat, Double maxLat, Double minLng, Double maxLng) {
        // 1. 오사카 조회 (Top 300)
        List<JobSummaryView> osakaJobs = osakaRepo.findTop300ByLatBetweenAndLngBetween(minLat, maxLat, minLng, maxLng);

        // 2. 도쿄 조회 (Top 300)
        List<JobSummaryView> tokyoJobs = tokyoRepo.findTop300ByLatBetweenAndLngBetween(minLat, maxLat, minLng, maxLng);

        // 3. 합치기
        List<JobSummaryView> allJobs = new ArrayList<>();
        allJobs.addAll(osakaJobs);
        allJobs.addAll(tokyoJobs);

        return allJobs;
    }
}