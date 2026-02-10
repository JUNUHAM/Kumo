package net.kumo.kumo.controller; // ※ 패키지명 빨간줄 뜨면 본인 걸로 수정!

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@RequiredArgsConstructor
@Controller
public class HomeController {
    @GetMapping({ "", "/" })
    public String home() {
        return "home";
    }

    @GetMapping("/info")
    public String info() {
        return "NonLoginView/info";
    }

}