package net.kumo.kumo.repository;

import net.kumo.kumo.domain.entity.ScrapEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScrapRepository extends JpaRepository<ScrapEntity, Long> {
	
	// 1. 유저ID와 공고ID로 스크랩 여부 확인
	boolean existsByUserIdAndJobPostId(Long userId, Long jobPostId);
	
	// 2. 유저ID와 공고ID로 스크랩 내역 삭제
	void deleteByUserIdAndJobPostId(Long userId, Long jobPostId);
}