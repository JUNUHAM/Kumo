package net.kumo.kumo.repository;

import net.kumo.kumo.domain.entity.ApplicationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<ApplicationEntity, Long> {
	List<ApplicationEntity> findBySeeker_Id(Long seekerId);
	List<ApplicationEntity> findByJobPosting_Id(Long jobPostId);
	Optional<ApplicationEntity> findByJobPosting_IdAndSeeker_Id(Long jobPostId, Long seekerId);
}