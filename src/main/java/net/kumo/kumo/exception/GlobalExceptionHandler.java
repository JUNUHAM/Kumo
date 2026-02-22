package net.kumo.kumo.exception;

import lombok.extern.slf4j.Slf4j;
import net.kumo.kumo.domain.dto.ErrorResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
	// ==========================================
	// 1. 401 Unauthorized (비로그인, 권한 부족)
	// ==========================================
	@ExceptionHandler(UnauthorizedException.class)
	public ResponseEntity<ErrorResponseDTO> handleUnauthorizedException(UnauthorizedException e) {
		log.warn("🚨 [401 Unauthorized] {}", e.getMessage());
		ErrorResponseDTO response = ErrorResponseDTO.builder()
				.status(HttpStatus.UNAUTHORIZED.value())
				.error(HttpStatus.UNAUTHORIZED.getReasonPhrase())
				.message(e.getMessage())
				.build();
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
	}
	
	// ==========================================
	// 2. 404 Not Found (존재하지 않는 데이터/공고 등)
	// ==========================================
	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ErrorResponseDTO> handleResourceNotFoundException(ResourceNotFoundException e) {
		log.warn("🚨 [404 Not Found] {}", e.getMessage());
		ErrorResponseDTO response = ErrorResponseDTO.builder()
				.status(HttpStatus.NOT_FOUND.value())
				.error(HttpStatus.NOT_FOUND.getReasonPhrase())
				.message(e.getMessage())
				.build();
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
	}
	
	// ==========================================
	// 3. 404 Not Found (잘못된 URL 요청 - Spring 기본 예외)
	// ==========================================
	@ExceptionHandler(NoHandlerFoundException.class)
	public ResponseEntity<ErrorResponseDTO> handleNoHandlerFoundException(NoHandlerFoundException e) {
		log.warn("🚨 [404 잘못된 URL 요청] {}", e.getRequestURL());
		ErrorResponseDTO response = ErrorResponseDTO.builder()
				.status(HttpStatus.NOT_FOUND.value())
				.error("API Endpoint Not Found")
				.message("요청하신 URL을 찾을 수 없습니다.")
				.build();
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
	}
	
	// ==========================================
	// 4. 500 Internal Server Error (최후의 보루: 예상치 못한 모든 에러)
	// ==========================================
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponseDTO> handleAllUncaughtException(Exception e) {
		log.error("🔥 [500 Internal Server Error] 예상치 못한 서버 에러 발생!", e);
		ErrorResponseDTO response = ErrorResponseDTO.builder()
				.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
				.error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
				.message("서버에 일시적인 문제가 발생했습니다. 잠시 후 다시 시도해주세요.")
				.build();
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	}
}