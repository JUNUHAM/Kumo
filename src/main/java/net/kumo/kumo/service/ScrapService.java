package net.kumo.kumo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.kumo.kumo.domain.dto.ScrapDTO;
import net.kumo.kumo.domain.entity.ScrapEntity;
import net.kumo.kumo.repository.ScrapRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScrapService {
	
	private final ScrapRepository scrapRepository;
	
	/**
	 * 스크랩(찜하기) 토글 로직
	 * @return true: 스크랩 추가됨, false: 스크랩 삭제됨
	 */
	@Transactional // 삭제 및 저장 작업이 있으므로 트랜잭션 필수!
	public boolean toggleScrap(ScrapDTO scrapDTO) {
		Long userId = scrapDTO.getUserId();
		Long jobPostId = scrapDTO.getTargetPostId();
		
		log.info("📌 스크랩 토글 요청 - UserId: {}, JobPostId: {}", userId, jobPostId);
		
		// 1. 이미 스크랩한 공고인지 확인 (JPA 메서드 활용)
		if (scrapRepository.existsByUserIdAndJobPostId(userId, jobPostId)) {
			// 2-A. 이미 있다면 삭제 (스크랩 취소)
			scrapRepository.deleteByUserIdAndJobPostId(userId, jobPostId);
			log.info("🗑️ 스크랩 취소 완료");
			return false;
		} else {
			// 2-B. 없다면 새로 저장 (스크랩 추가)
			ScrapEntity newScrap = ScrapEntity.builder()
					.userId(userId)
					.jobPostId(jobPostId)
					.build();
			scrapRepository.save(newScrap);
			log.info("⭐ 스크랩 등록 완료");
			return true;
		}
	}
	
	/**
	 * 사용자가 특정 공고를 이미 스크랩했는지 단순 확인합니다. (상세페이지 로딩용)
	 */
	@Transactional(readOnly = true) // 단순 조회용이므로 readOnly
	public boolean checkIsScraped(Long userId, Long jobPostId) {
		return scrapRepository.existsByUserIdAndJobPostId(userId, jobPostId);
	}
}