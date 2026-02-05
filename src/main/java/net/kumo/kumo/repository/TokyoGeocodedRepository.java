package net.kumo.kumo.repository;

import net.kumo.kumo.domain.dto.projection.JobSummaryView;
import net.kumo.kumo.domain.entity.TokyoGeocodedEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TokyoGeocodedRepository extends BaseRepository<TokyoGeocodedEntity> {

    List<JobSummaryView> findTop300ByLatBetweenAndLngBetween(
            Double minLat, Double maxLat, Double minLng, Double maxLng
    );
}
