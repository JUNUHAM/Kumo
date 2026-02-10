package net.kumo.kumo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/seeker") // 이 컨트롤러의 모든 주소는 앞에 /seeker 가 붙습니다.
public class SeekerController {

    // 접속 주소: localhost:8080/seeker/mypage
    @GetMapping("/mypage")
    public String myPage() {
        return "my_page_my_profile"; // 템플릿 파일 이름
    }

    // SeekerController.java 내부

    @GetMapping("/mypage/modify") // 주소: /seeker/mypage/modify
    public String myPageModify() {
        return "my_page_my_profile_modify";
    }

    // SeekerController.java 내부

    @GetMapping("/resume") // 주소: /seeker/resume
    public String seekerResume() {
        return "my_page_resume";
    }

    // SeekerController.java 내부에 추가

    @GetMapping("/resume/check") // 주소: /seeker/resume/check
    public String resumeCheck() {
        return "my_page_resume_check";
    }
}
