package net.kumo.kumo.repository;

import net.kumo.kumo.domain.entity.JobImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobImageRepository extends JpaRepository<JobImageEntity, Long> {
	List<JobImageEntity> findByJobPosting_Id(Long jobPostId);
}