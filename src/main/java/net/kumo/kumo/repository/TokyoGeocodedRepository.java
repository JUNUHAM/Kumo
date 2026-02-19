package net.kumo.kumo.repository;

import net.kumo.kumo.domain.dto.projection.JobSummaryView;
import net.kumo.kumo.domain.entity.TokyoGeocodedEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TokyoGeocodedRepository extends BaseRepository<TokyoGeocodedEntity> {

    // ★ 지도 쿼리 (Projection 사용)
    List<JobSummaryView> findTop300ByLatBetweenAndLngBetween(
            Double minLat, Double maxLat, Double minLng, Double maxLng
    );

    // ★ [추가] 도넛 차트용: 구(Ward)별 카운트 (도쿄는 컬럼명이 다름)
    @Query("SELECT t.wardCityJp, COUNT(t) FROM TokyoGeocodedEntity t GROUP BY t.wardCityJp HAVING t.wardCityJp IS NOT NULL")
    List<Object[]> countByWard();
}