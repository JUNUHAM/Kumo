package net.kumo.kumo.repository;

import net.kumo.kumo.domain.entity.ScrapEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ScrapRepository extends JpaRepository<ScrapEntity, Long> {
	List<ScrapEntity> findByUser_Id(Long userId);
	Optional<ScrapEntity> findByUser_IdAndJobPosting_Id(Long userId, Long jobPostId);
}