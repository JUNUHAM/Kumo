package net.kumo.kumo.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.kumo.kumo.service.RecruiterService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("Recruiter")
@Controller
public class RecruiterController {
	private final RecruiterService rs;
	
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
		model.addAttribute("currentMenu", "jobManage"); // 공고관리 관리 식별자
		return "recruiterView/jobManage";
	}
}