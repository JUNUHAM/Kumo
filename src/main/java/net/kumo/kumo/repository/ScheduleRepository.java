package net.kumo.kumo.repository;

import net.kumo.kumo.domain.entity.ScheduleEntity;
import net.kumo.kumo.domain.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScheduleRepository extends JpaRepository<ScheduleEntity, Long> {
	// 특정 유저(리크루터)의 일정만 싹 가져오는 마법의 메서드
	List<ScheduleEntity> findByUser(UserEntity user);
}