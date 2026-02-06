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

    /**
     * 홈 메뉴 컨트롤러
     * 
     * @param model
     * @return
     */
    @GetMapping("Main")
    public String Main(Model model) {
        model.addAttribute("currentMenu", "home"); // 사이드바 선택(홈 메뉴)
        return "recruiterView/main";
    }

    /**
     * 지원자 관리 컨트롤러
     * 
     * @param model
     * @return
     */
    @GetMapping("ApplicantInfo")
    public String ApplicantInfo(Model model) {
        model.addAttribute("currentMenu", "applicants"); // 사이드바 선택((지원자 관리)
        return "recruiterView/applicantInfo";
    }

    /**
     * 공고 관리 컨트롤러
     * 
     * @param model
     * @return
     */
    @GetMapping("JobManage")
    public String JobManage(Model model) {
        model.addAttribute("currentMenu", "jobManage"); // 사이드바 선택(공고 관리)
        return "recruiterView/jobManage";
    }

    /**
     * 캘린더 컨트롤러
     * 
     * @param model
     * @return
     */
    @GetMapping("Calendar")
    public String Calender(Model model) {
        model.addAttribute("currentMenu", "calendar"); // 사이드바 선택(캘린더)
        return "recruiterView/calendar";
    }

    /**
     * 내 계정(settings) 컨트롤러
     * 
     * @param model
     * @return
     */
    @GetMapping("Settings")
    public String Settings(Model model) {
        model.addAttribute("currentMenu", "settings"); // 사이드바 선택(내 계정)
        return "recruiterView/settings";
    }

    /**
     * 공고 등록 컨트롤러
     * 
     * @param model
     * @return
     */
    @GetMapping("JobPosting")
    public String JobPosting(Model model) {
        return "recruiterView/jobPosting";
    }

    // // info 테스트
    @GetMapping("Info")
    public String Info(Model model) {
        return "recruiterView/info";
    }
}