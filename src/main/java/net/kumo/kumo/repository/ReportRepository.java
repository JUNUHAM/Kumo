package net.kumo.kumo.repository;

import net.kumo.kumo.domain.entity.ReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<ReportEntity, Long> {

    /**
     * [관리자 페이지용]
     * 모든 신고 내역을 '최신순'으로 조회합니다.
     */
    List<ReportEntity> findAllByOrderByCreatedAtDesc();

    /**
     * [선택 사항]
     * 특정 상태(예: PENDING, BLOCKED)인 신고만 최신순으로 조회
     */
    List<ReportEntity> findByStatusOrderByCreatedAtDesc(String status);

    /**
     * [중복 신고 방지용]
     * 한 유저가 같은 게시글을 중복 신고했는지 체크
     * * [수정] 메소드 이름과 실제 엔티티 필드명(userId)의 불일치를 해결하기 위해 JPQL 사용
     * r.reporter.userId : ReportEntity -> UserEntity -> userId 필드 참조
     */
    @Query("SELECT COUNT(r) > 0 FROM ReportEntity r WHERE r.reporter.userId = :reporterId AND r.targetPostId = :targetPostId")
    boolean existsByReporterIdAndTargetPostId(@Param("reporterId") Long reporterId,
                                              @Param("targetPostId") Long targetPostId);
}