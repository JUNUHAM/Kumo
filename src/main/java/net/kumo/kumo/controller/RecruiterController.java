package net.kumo.kumo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// 구인자 페이지 컨트롤러
@Slf4j
@RequiredArgsConstructor
@RequestMapping("Recruiter")
@Controller
public class RecruiterController {

    @GetMapping("Main")
    public String Main(Model model) {
        model.addAttribute("currentMenu", "home"); // 홈 메뉴 식별자
        return "recruiterView/main";
    }

    @GetMapping("ApplicantInfo")
    public String ApplicantInfo(Model model) {
        model.addAttribute("currentMenu", "applicants"); // 지원자 관리 식별자
        return "recruiterView/applicantInfo";
    }

    @GetMapping("JobManage")
    public String JobManage(Model model) {
        model.addAttribute("currentMenu", "jobManage"); // 지원자 관리 식별자
        return "recruiterView/jobManage";
    }

    @GetMapping("Calendar")
    public String Calender(Model model) {
        model.addAttribute("currentMenu", "calendar"); // 지원자 관리 식별자
        return "recruiterView/calendar";
    }

    @GetMapping("Settings")
    public String Settings(Model model) {
        model.addAttribute("currentMenu", "settings"); // 지원자 관리 식별자
        return "recruiterView/settings";
    }
}