package net.kumo.kumo.repository;

import net.kumo.kumo.domain.dto.projection.JobSummaryView;
import net.kumo.kumo.domain.entity.BaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;

@NoRepositoryBean // 중요: 이 인터페이스로 인스턴스를 만들지 않음
public interface BaseRepository<T extends BaseEntity> extends JpaRepository<T, Long> {

    // 4개 테이블 모두에서 공통으로 쓸 쿼리 메소드 정의
    // JPA Projection 을 통해서 필요한 컬럼 일부만 추출 (전체 정보는 FindById로 추출)
    // 지도 상 맵 마커 표시할 때도 이 메소드를 사용해서 위치정보 추출
    List<JobSummaryView> findByCompanyNameContaining(String name);
}