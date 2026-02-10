package net.kumo.kumo.controller; // 패키지명 확인! (본인 프로젝트에 맞게)

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    // ==============================
    // 1. 로그인 화면
    // ==============================
    @GetMapping("/login")
    public String loginPage() {
        return "login"; // login.html
    }

    // ==============================
    // 2. 회원가입 선택 화면 (구름 아이콘)
    // ==============================
    @GetMapping("/signup")
    public String joinPage() {
        return "join"; // join.html
    }

    // ==============================
    // 3. 구직자 회원가입 화면
    // ==============================
    @GetMapping("/signup/seeker")
    public String seekerSignupForm() {
        // 아까는 "join"이었지만, 이제 진짜 파일이 생겼으니 이름을 바꿔줍니다.
        // templates 폴더의 signup_seeker.html을 보여줍니다.
        return "sign_up_seeker";
    }

    // ==============================
    // 4. 구인자 회원가입 화면
    // ==============================
    @GetMapping("/signup/recruiter")
    public String recruiterSignupForm() {
        // 구인자 파일은 아직 안 만들었으니, 일단 선택 화면으로 돌려보냅니다.
        // 나중에 파일 만들면 여기도 "signup_recruiter"로 바꾸면 됩니다.
        return "sign_up_recruiter";
    }
}