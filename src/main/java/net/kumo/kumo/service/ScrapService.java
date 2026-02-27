package net.kumo.kumo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.kumo.kumo.domain.dto.JobDetailDTO;
import net.kumo.kumo.domain.dto.ScrapDTO;
import net.kumo.kumo.domain.entity.BaseEntity;
import net.kumo.kumo.domain.entity.ScrapEntity;
import net.kumo.kumo.domain.entity.UserEntity;
import net.kumo.kumo.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScrapService {
	
	private final ScrapRepository scrapRepository;
	private final UserRepository userRepository; // 🌟 유저 조회를 위해 서비스에 주입
	
	// 🌟 [추가] 실제 공고 데이터를 조회하기 위해 4개의 테이블 Repository 주입
	private final OsakaGeocodedRepository osakaRepo;
	private final TokyoGeocodedRepository tokyoRepo;
	private final OsakaNoGeocodedRepository osakaNoRepo;
	private final TokyoNoGeocodedRepository tokyoNoRepo;
	
	/**
	 * 스크랩(찜하기) 토글 로직
	 * @param scrapDTO 프론트에서 넘어온 공고 정보
	 * @param loginEmail 로그인한 사용자의 이메일 (Security Principal)
	 * @return true: 스크랩 추가됨, false: 스크랩 삭제됨
	 */
	@Transactional
	public boolean toggleScrap(ScrapDTO scrapDTO, String loginEmail) {
		
		UserEntity user = userRepository.findByEmail(loginEmail)
				.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
		
		Long userId = user.getUserId();
		Long jobPostId = scrapDTO.getTargetPostId();
		String source = scrapDTO.getTargetSource(); // 🌟 추가: DTO에서 소스(TOKYO 등) 가져오기
		
		log.info("📌 스크랩 토글 요청 - UserId: {}, JobPostId: {}, Source: {}", userId, jobPostId, source);
		
		// 🌟 수정: 메서드명 변경 (AndSource 추가) 및 source 파라미터 추가
		if (scrapRepository.existsByUserIdAndJobPostIdAndSource(userId, jobPostId, source)) {
			scrapRepository.deleteByUserIdAndJobPostIdAndSource(userId, jobPostId, source);
			log.info("🗑️ 스크랩 취소 완료");
			return false;
		} else {
			ScrapEntity newScrap = ScrapEntity.builder()
					.userId(userId)
					.jobPostId(jobPostId)
					.source(source) // 🌟 빌더에 추가
					.build();
			scrapRepository.save(newScrap);
			log.info("⭐ 스크랩 등록 완료");
			return true;
		}
	}
	
	/**
	 * 사용자가 특정 공고를 이미 스크랩했는지 단순 확인합니다. (상세페이지 로딩용)
	 */
	@Transactional(readOnly = true)
	public boolean checkIsScraped(Long userId, Long jobPostId, String source) {
		return scrapRepository.existsByUserIdAndJobPostIdAndSource(userId, jobPostId, source);
	}
	
	/**
	 * 🌟 [NEW] 유저의 찜 목록을 JobDetailDTO 리스트로 변환하여 반환
	 */
	@Transactional(readOnly = true)
	public List<JobDetailDTO> getScrapedJobsList(String loginEmail, String lang) {
		// 1. 유저 조회
		UserEntity user = userRepository.findByEmail(loginEmail)
				.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
		
		// 2. 해당 유저의 찜 내역 최신순 조회
		List<ScrapEntity> scraps = scrapRepository.findByUserIdOrderByCreatedAtDesc(user.getUserId());
		List<JobDetailDTO> result = new ArrayList<>();
		
		// 3. 찜 내역을 돌면서 실제 공고 데이터 가져오기
		for (ScrapEntity scrap : scraps) {
			Long id = scrap.getJobPostId();
			String source = scrap.getSource();
			BaseEntity entity = null;
			
			// 출처(source)에 따라 알맞은 테이블에서 조회
			if ("OSAKA".equalsIgnoreCase(source)) {
				entity = osakaRepo.findById(id).orElse(null);
			} else if ("TOKYO".equalsIgnoreCase(source)) {
				entity = tokyoRepo.findById(id).orElse(null);
			} else if ("OSAKA_NO".equalsIgnoreCase(source)) {
				entity = osakaNoRepo.findById(id).orElse(null);
			} else if ("TOKYO_NO".equalsIgnoreCase(source)) {
				entity = tokyoNoRepo.findById(id).orElse(null);
			}
			
			// 공고가 삭제되지 않고 존재한다면 DTO로 변환하여 리스트에 추가
			if (entity != null) {
				result.add(new JobDetailDTO(entity, lang, source));
			}
		}
		
		return result;
	}
}