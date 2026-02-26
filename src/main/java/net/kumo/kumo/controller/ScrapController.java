package net.kumo.kumo.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.kumo.kumo.domain.dto.JobDetailDTO;
import net.kumo.kumo.domain.dto.ScrapDTO;
import net.kumo.kumo.service.ScrapService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/scraps")
@RequiredArgsConstructor
public class ScrapController {
	
	private final ScrapService scrapService;
	
	/**
	 * 특정 공고에 대한 사용자의 스크랩 상태를 토글(추가 또는 취소)합니다.
	 */
	@PostMapping
	public ResponseEntity<ScrapDTO> toggleScrap(@RequestBody ScrapDTO scrapDTO, Principal principal) {
		
		// 1. 요청 자격 검증 (문지기 역할 충실)
		if (principal == null) {
			log.warn("🚨 스크랩 실패: 로그인하지 않은 사용자의 접근");
			throw new IllegalArgumentException("로그인이 필요합니다.");
		}
		
		// 2. 신분증(Principal)에서 이메일만 쏙 빼서 서비스팀에 서류(dto)와 함께 전달!
		String loginEmail = principal.getName();
		boolean isScraped = scrapService.toggleScrap(scrapDTO, loginEmail);
		
		// 3. 결과 포장해서 프론트로 반환
		scrapDTO.setScraped(isScraped);
		return ResponseEntity.ok(scrapDTO);
	}
	
	/**
	 * 🌟 [NEW] 로그인한 사용자의 찜한 공고 리스트 반환
	 */
	@GetMapping
	public ResponseEntity<List<JobDetailDTO>> getScrapedJobs(
			Principal principal,
			@RequestParam(defaultValue = "kr") String lang) {
		
		if (principal == null) {
			throw new IllegalArgumentException("로그인이 필요합니다.");
		}
		
		String loginEmail = principal.getName();
		List<JobDetailDTO> scrapedJobs = scrapService.getScrapedJobsList(loginEmail, lang);
		
		log.info("🎯 찜한 목록 조회 완료 - 유저: {}, 개수: {}개", loginEmail, scrapedJobs.size());
		
		return ResponseEntity.ok(scrapedJobs);
	}
}