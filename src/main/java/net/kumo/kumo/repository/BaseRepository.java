package net.kumo.kumo.repository;

import net.kumo.kumo.domain.dto.projection.JobSummaryView;
import net.kumo.kumo.domain.entity.BaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;    // 검색을 위한 동적 쿼리 실행을 위하여 상속 추가
import org.springframework.data.repository.NoRepositoryBean;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@NoRepositoryBean
public interface BaseRepository<T extends BaseEntity> extends JpaRepository<T, Long>, JpaSpecificationExecutor<T> {

    // (기존) 상세 조회용
    Optional<T> findByDatanum(Long datanum);
    List<JobSummaryView> findByCompanyNameContaining(String name);

    // ★ [추가] 통계용: 특정 날짜 이후 등록된 공고 개수 (신규 공고 카드용)
    long countByCreatedAtAfter(LocalDateTime date);

    // ★ [추가] 통계용: 특정 날짜 이후 등록된 공고 목록 (주간 차트용)
    // -> BaseEntity에 createdAt이 있으므로 자식들도 자동 적용됨
    List<T> findByCreatedAtAfter(LocalDateTime date);
}