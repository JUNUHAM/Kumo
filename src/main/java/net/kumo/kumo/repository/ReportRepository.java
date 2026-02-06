package net.kumo.kumo.repository;

import net.kumo.kumo.domain.entity.Enum;
import net.kumo.kumo.domain.entity.ReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportRepository extends JpaRepository<ReportEntity, Long> {
	List<ReportEntity> findByReporter_Id(Long reporterId);
	List<ReportEntity> findByTargetPost_Id(Long jobPostId);
	List<ReportEntity> findByStatus(Enum.ReportStatus status);
}