package net.kumo.kumo.domain.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 프론트 엔드로 반환할 공통 에러 응답 포맷
 */
@Getter
@Builder // 🌟 다시 추가! (GlobalExceptionHandler의 .builder() 에러 해결)
public class ErrorResponseDTO {
	
	// 🌟 @Builder.Default: 빌더 패턴을 쓸 때 이 기본값(now)을 무시하지 말고 꼭 써달라는 롬복의 명령어입니다.
	@Builder.Default
	private final LocalDateTime timestamp = LocalDateTime.now();
	
	private final int status;       // HTTP 상태 코드 (예: 400, 401, 404)
	private final String error;     // 에러 종류 (예: "Unauthorized")
	private final String message;   // 상세 메시지 (예: "로그인이 필요합니다.")
}