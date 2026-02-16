package net.kumo.kumo.repository;

import net.kumo.kumo.domain.dto.projection.JobSummaryView;
import net.kumo.kumo.domain.entity.OsakaGeocodedEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OsakaGeocodedRepository extends BaseRepository<OsakaGeocodedEntity> {

    // ★ 지도 쿼리 (Projection 사용)
    // 화면 범위(Lat, Lng) 내의 데이터를 최대 300개만 가져옴
    List<JobSummaryView> findTop300ByLatBetweenAndLngBetween(
            Double minLat, Double maxLat, Double minLng, Double maxLng
    );

    // ★ [추가] 도넛 차트용: 구(Ward)별 카운트
    @Query("SELECT o.wardJp, COUNT(o) FROM OsakaGeocodedEntity o GROUP BY o.wardJp HAVING o.wardJp IS NOT NULL")
    List<Object[]> countByWard();
}