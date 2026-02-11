package net.kumo.kumo.controller;


import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.kumo.kumo.service.EmailService;
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
	
	@PostMapping("/api/mail/send")
	public ResponseEntity<String> sendCertificationMail(@RequestBody Map<String, String> request, HttpSession session) {
		String email = request.get("email");
		
		// 1. 이메일 유효성 검사 (간단하게)
		if (email == null || email.isEmpty()) {
			return ResponseEntity.badRequest().body("이메일을 입력해주세요.");
		}
		
		// 2. 서비스 호출 (메일 발송 + 인증번호 반환)
		String code = emailService.sendCertigicationMail(email);
		
		// 3. ★ 핵심: 인증번호를 세션에 저장 (나중에 맞는지 확인하려고)
		// "verifyCode"라는 이름표를 붙여서 저장해둠
		session.setAttribute("verifyCode", code);
		
		// (선택) 세션 유지 시간 설정: 180초 (3분) 지나면 인증번호 삭제됨
		session.setMaxInactiveInterval(180);
		
		
		return ResponseEntity.ok("인증번호가 발송되었습니다.");
	}
	
	@PostMapping("/api/mail/check")
	public boolean checkCode(@RequestBody Map<String, String> requset, HttpSession session) {
		// 1. 일단 js로 부터 받은 이메일 인증 꺼내기
		String inputCode = requset.get("code");
		
		//2. 서버 세션에서 저장해둔 값 꺼내기
		String sessionCode = (String) session.getAttribute("verifyCode");
		
		//3. 값이 같은지 비교
		if (sessionCode != null && sessionCode.equals(inputCode)) {
			session.removeAttribute("verifyCode");
			return true;
		}
		return false;
	}
}
