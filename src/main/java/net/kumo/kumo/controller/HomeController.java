package net.kumo.kumo.controller; // ※ 패키지명 빨간줄 뜨면 본인 걸로 수정!

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    // 1. 우리가 주소창에 "localhost:8080" 치고 들어오면 여기로 옵니다.
    @GetMapping("/")
    public String home() {
        // "index"라고 했던 건 잊어버려!
        // templates 폴더 > seekerView 폴더 > header.html을 보여줘라!
        return "home";
    }

    // 2. 회사 정보 페이지 (✨여기가 핵심!)
    // localhost:8080/company_info 접속 시 실행
    @GetMapping("/company_info")
    public String companyInfo() {
        return "kumo_info"; // templates/kumo_info.html 파일을 찾아라!
    }
}