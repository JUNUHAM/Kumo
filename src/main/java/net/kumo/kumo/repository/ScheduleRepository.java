package net.kumo.kumo.repository;

import net.kumo.kumo.domain.entity.ScheduleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ScheduleRepository extends JpaRepository<ScheduleEntity, Long> {
	List<ScheduleEntity> findByRecruiter_Id(Long recruiterId);
	
	// 일정 범위 조회(겹치는 구간)
	List<ScheduleEntity> findByRecruiter_IdAndStartDatetimeLessThanEqualAndEndDatetimeGreaterThanEqual(
			Long recruiterId, LocalDateTime end, LocalDateTime start
	);
}