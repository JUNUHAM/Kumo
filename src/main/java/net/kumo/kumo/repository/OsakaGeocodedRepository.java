package net.kumo.kumo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import net.kumo.kumo.domain.dto.projection.JobSummaryView;
import net.kumo.kumo.domain.entity.OsakaGeocodedEntity;

@Repository
public interface OsakaGeocodedRepository extends BaseRepository<OsakaGeocodedEntity> {

    // 🌟 [추가] 최신순 조회 (createdAt이 엔티티에 있다면)
    List<OsakaGeocodedEntity> findAllByOrderByCreatedAtDesc();

    // 🌟 [추가] row_no 최대값 조회 (새 글 등록 시 번호 매기기용)
    @Query("SELECT MAX(o.rowNo) FROM OsakaGeocodedEntity o")
    Integer findMaxRowNo();

    // ★ 지도 쿼리 (기존 유지)
    List<JobSummaryView> findTop300ByLatBetweenAndLngBetween(
            Double minLat, Double maxLat, Double minLng, Double maxLng);

    // ★ 도넛 차트용 (기존 유지)
    @Query("SELECT o.wardJp, COUNT(o) FROM OsakaGeocodedEntity o GROUP BY o.wardJp HAVING o.wardJp IS NOT NULL")
    List<Object[]> countByWard();
}