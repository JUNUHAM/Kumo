package net.kumo.kumo.controller;


import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.kumo.kumo.domain.dto.FindPwDTO;
import net.kumo.kumo.service.EmailService;
import net.kumo.kumo.service.LoginService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Controller
public class MailController {
	private final EmailService emailService;
	private final LoginService loginService;
	
	
	@PostMapping("/api/mail/send")
	public ResponseEntity<String> sendCertificationMail(@RequestBody FindPwDTO findPwDTO , HttpSession session) {
		// 1. DTO에서 데이터 추출
		String name = findPwDTO.getName();
		String contact = findPwDTO.getContact(); // DTO 구조에 따라 request.getContact() 사용
		String email = findPwDTO.getEmail();
		String role = findPwDTO.getRole();
		
		log.info("받아온 것 {},{},{},{}",name,contact,email,role);
		
		// 2. 기초 유효성 검사 (빈 값 체크)
		if (email == null || email.isEmpty()) {
			return ResponseEntity.badRequest().body("EMPTY_EMAIL");
		}
		
		// 3. DB 정보 일치 여부 확인 (이름, 연락처, 이메일, 역할)
		// 이메일 발송 전, 실제 해당 정보를 가진 사용자가 있는지 검증합니다.
		boolean isUserValid = loginService.emailVerify(name, contact, email, role);
		
		log.info("트루 펄스? {}",isUserValid);
		
		if (!isUserValid) {
			// 정보가 일치하지 않을 때 반환할 코드 (JS의 msg-fail-default와 매칭)
			return ResponseEntity.badRequest().body("USER_NOT_FOUND");
		}
		
		try {
			// 4. 서비스 호출 (메일 발송 + 인증번호 생성)
			String code = emailService.sendCertigicationMail(email);
			
			// 5. 인증번호 세션 저장 및 만료 시간 설정
			session.setAttribute("verifyCode", code);
			session.setMaxInactiveInterval(180); // 3분 후 세션 만료
			
			return ResponseEntity.ok("SUCCESS");
			
		} catch (Exception e) {
			// 메일 서버 오류 등 예외 발생 시
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("MAIL_SEND_ERROR");
		}
	}
	
	@PostMapping("/api/mail/check")
	public ResponseEntity<Boolean> checkCode(@RequestBody Map<String, String> requset, HttpSession session) {
		// 1. 일단 js로 부터 받은 이메일 인증 꺼내기
		String inputCode = requset.get("code");
		
		//2. 서버 세션에서 저장해둔 값 꺼내기
		String sessionCode = (String) session.getAttribute("verifyCode");
		
		//3. 값이 같은지 비교
		if (sessionCode != null && sessionCode.equals(inputCode)) {
			session.removeAttribute("verifyCode");
			log.info("sessionCode : {}, inputCode : {} ",sessionCode,inputCode);
			
			return ResponseEntity.ok(true);
			
		}
		return ResponseEntity.ok(false);
	}
}