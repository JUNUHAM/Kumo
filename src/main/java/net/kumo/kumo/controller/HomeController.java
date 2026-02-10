package net.kumo.kumo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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