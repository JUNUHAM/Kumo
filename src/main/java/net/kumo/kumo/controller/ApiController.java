package net.kumo.kumo.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.kumo.kumo.domain.dto.FindIdDTO;
import net.kumo.kumo.domain.dto.ProfileImageUploadDTO;
import net.kumo.kumo.service.LoginService;
import net.kumo.kumo.service.SeekerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ApiController {
	private final LoginService LoginService;
	private final SeekerService seekerService;
	
	//회원가입할때 닉네임 중복확인
	@PostMapping("/api/check/nickname")
	public ResponseEntity<Boolean> checkNickname(@RequestBody Map<String, String> request) {
		String nickname = request.get("nickname");
		// 존재하면 true, 없으면 false 반환
		boolean exists = LoginService.existsByNickname(nickname);
		return ResponseEntity.ok(exists);
	}
	
	
	// 회원가입 할때 이메일 중복확인
	@PostMapping("/api/check/email")
	public ResponseEntity<Boolean> checkEmail(@RequestBody Map<String, String> request) {
		String email = request.get("email");
		// 존재하면 true, 없으면 false 반환
		boolean exists = LoginService.existsByEmail(email);
		return ResponseEntity.ok(exists);
	}
	
	@PostMapping("/api/findId")
	public ResponseEntity<Map<String, Object>> findIdProc(@RequestBody FindIdDTO findIdDTO) {
		log.info("findId 요청 수신: {}", findIdDTO);
		Map<String, Object> response = new HashMap<>();
		
		// 1. 서비스 호출 (DB 조회)
		// 서비스는 일치하는게 없으면 null을 리턴한다고 가정
		String foundEmail = LoginService.findId(findIdDTO);
		
		if (foundEmail != null) {
			// 2. 이메일 마스킹 처리 (보안)
			String maskedEmail = maskEmail(foundEmail);
			
			// 3. 성공 응답 구성
			response.put("status", "success");
			response.put("email", maskedEmail);
			response.put("message", "일치하는 정보를 찾았습니다.");
		} else {
			// 4. 실패 응답 구성
			response.put("status", "fail");
			response.put("message", "일치하는 회원 정보가 없습니다.");
		}
		
		return ResponseEntity.ok(response);
	}
	
	
	private String maskEmail(String email) {
		if (email == null || !email.contains("@")) {
			return email; // 방어 코드
		}
		
		String[] parts = email.split("@");
		String id = parts[0];
		String domain = parts[1];
		int len = id.length();
		
		String maskedId;
		
		if (len <= 2) {
			// 2글자 이하: 앞 1글자만 노출 (ex: k*@gmail.com)
			maskedId = id.charAt(0) + "*".repeat(len - 1);
		} else if (len == 3) {
			// 3글자: 앞뒤 1글자 노출 (ex: k*o@gmail.com)
			maskedId = id.charAt(0) + "*" + id.charAt(2);
		} else {
			// 4글자 이상: 앞 3글자 + **** + 뒤 2글자 노출
			// (ex: develop -> dev****op)
			String head = id.substring(0, 3);
			String tail = id.substring(len - 2);
			maskedId = head + "****" + tail;
		}
		
		return maskedId + "@" + domain;
	}
	
	@PostMapping("/api/profileImage")
	public ResponseEntity<String> uploadProfileImage(
			@ModelAttribute ProfileImageUploadDTO dto,
			@AuthenticationPrincipal UserDetails details
			){
		try{
			
			log.info("ㅅㅂ1: {}", dto);
			String newImagePath = seekerService.updateProfileImage(details.getUsername(),dto.getProfileImage());
			
			log.info("ㅅㅂ2: {}", dto);
			return ResponseEntity.ok(newImagePath);
		}catch (Exception e){
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("파일 업로드 실패");
		}
		
		
	}
	
	
	
}
