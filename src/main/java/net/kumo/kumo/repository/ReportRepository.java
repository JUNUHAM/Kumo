package net.kumo.kumo.repository;

import net.kumo.kumo.domain.entity.ReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<ReportEntity, Long> {

    /**
     * [관리자 페이지용]
     * 모든 신고 내역을 '최신순'으로 조회합니다.
     * AdminService.getAllReports() 에서 사용됩니다.
     */
    List<ReportEntity> findAllByOrderByCreatedAtDesc();

    /**
     * [선택 사항]
     * 특정 상태(예: PENDING, BLOCKED)인 신고만 최신순으로 조회
     * 나중에 '처리 안 된 신고만 보기' 필터 기능 구현 시 사용
     */
    List<ReportEntity> findByStatusOrderByCreatedAtDesc(String status);

    /**
     * [중복 신고 방지용]
     * 한 유저가 같은 게시글을 중복 신고했는지 체크
     */
    boolean existsByReporterIdAndTargetPostId(Long reporterId, Long targetPostId);
}