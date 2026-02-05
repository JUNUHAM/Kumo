package net.kumo.kumo.repository;

import net.kumo.kumo.domain.dto.projection.JobSummaryView;
import net.kumo.kumo.domain.entity.OsakaGeocodedEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OsakaGeocodedRepository extends BaseRepository<OsakaGeocodedEntity> {

    // BaseOsakaRepository에 있는 기능은 자동으로 다 가짐

    List<JobSummaryView> findTop300ByLatBetweenAndLngBetween(
            Double minLat, Double maxLat, Double minLng, Double maxLng
    );
}