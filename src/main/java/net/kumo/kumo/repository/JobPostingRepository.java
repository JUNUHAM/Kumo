package net.kumo.kumo.repository;

import net.kumo.kumo.domain.entity.Enum;
import net.kumo.kumo.domain.entity.JobPostingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobPostingRepository extends JpaRepository<JobPostingEntity, Long> {
	List<JobPostingEntity> findByStatus(Enum.JobStatus status);
	List<JobPostingEntity> findByRecruiter_Id(Long recruiterId);
	List<JobPostingEntity> findByRegion_Id(Long regionId);
}
