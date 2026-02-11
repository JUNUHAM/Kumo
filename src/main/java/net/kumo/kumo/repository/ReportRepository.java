package net.kumo.kumo.repository;

import net.kumo.kumo.domain.entity.ReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportRepository extends JpaRepository<ReportEntity, Long> {
    // 필요 시 특정 유저가 이미 신고했는지 확인하는 로직 등 추가 가능
}