package net.kumo.kumo.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.kumo.kumo.domain.dto.ScrapDTO;
import net.kumo.kumo.service.ScrapService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;

// TODO: 본인 프로젝트에 맞는 DTO 및 Exception 클래스 Import 필요
// import net.kumo.kumo.exception.UnauthorizedException;

/**
 * 구인 공고 스크랩(즐겨찾기/찜하기) 관련 API 요청을 처리하는 컨트롤러입니다.
 */
@Slf4j
@RestController
@RequestMapping("/api/scraps")
@RequiredArgsConstructor
public class ScrapController {
	
	private final ScrapService scrapService;
	
	/**
	 * 특정 공고에 대한 사용자의 스크랩 상태를 토글(추가 또는 취소)합니다.
	 *
	 * @param scrapDTO 프론트엔드에서 전달받은 스크랩 대상 공고 정보 (targetPostId 포함)
	 * @param session  현재 사용자의 세션 정보 (로그인 여부 확인용)
	 * @return 상태가 업데이트된 ScrapDTO 객체 (isScraped 변경 여부 포함)
	 * @throws IllegalArgumentException (또는 CustomException) 비로그인 상태일 경우 발생
	 */
	@PostMapping
	public ResponseEntity<ScrapDTO> toggleScrap(@RequestBody ScrapDTO scrapDTO, HttpSession session) {
		
		// 1. 세션에서 로그인 유저 정보 조회
		Object sessionUser = session.getAttribute("loginUser");
		
		// 2. 예외 던지기 (GlobalExceptionHandler에서 HTTP 401 처리하도록 위임)
		if (sessionUser == null) {
			log.warn("🚨 스크랩 실패: 로그인하지 않은 사용자의 접근");
			
			// 본인 프로젝트에서 사용하는 Custom Exception으로 변경하세요!
			throw new IllegalArgumentException("로그인이 필요합니다.");
			// throw new UnauthorizedException("로그인이 필요합니다.");
		}
		
		// 3. 유저 ID 매핑
		// UserDTO loginUser = (UserDTO) sessionUser;
		// scrapDTO.setUserId(loginUser.getUserId());
		
		// 🚨 실제 연결 전 임시 하드코딩 (위 주석 풀 때 삭제하세요)
		scrapDTO.setUserId(1L);
		
		// 4. 서비스 로직 실행 (토글 처리)
		boolean isScraped = scrapService.toggleScrap(scrapDTO);
		
		// 5. 응답 데이터 세팅 및 반환
		scrapDTO.setScraped(isScraped);
		return ResponseEntity.ok(scrapDTO);
	}
}