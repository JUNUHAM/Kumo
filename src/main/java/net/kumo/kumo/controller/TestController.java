package net.kumo.kumo.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import net.kumo.kumo.domain.entity.UserEntity; // User 엔티티 import
import net.kumo.kumo.repository.UserRepository; // User 리포지토리 import
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequiredArgsConstructor
public class TestController {

    private final UserRepository userRepository;

    // 브라우저에서 http://localhost:8080/test/login 입력 시 실행
    @GetMapping("/test/login")
    @ResponseBody
    public String forceLogin(HttpSession session) {

        // 1. DB에서 1번 유저(또는 테스트하고 싶은 유저)를 가져옴
        // (만약 DB에 유저가 없다면 먼저 회원가입을 하나 해주세요!)
        UserEntity testUser = userRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("테스트용 유저(ID=1)가 없습니다. 회원가입 먼저 해주세요."));

        // 2. 세션에 "loginUser"라는 이름으로 저장 (ReportController가 이걸 참조함)
        session.setAttribute("loginUser", testUser);

        return "테스트 로그인 완료! (User ID: " + testUser.getUserId() + ") - 이제 신고 기능을 테스트하세요.";
    }

    // 로그아웃 테스트용
    @GetMapping("/test/logout")
    @ResponseBody
    public String forceLogout(HttpSession session) {
        session.invalidate();
        return "로그아웃 완료";
    }
}