package net.kumo.kumo.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("Seeker")
@Slf4j
@RequiredArgsConstructor
@Controller
public class SeekerController {
	
	@GetMapping("/mypage")
	public String SeekerMypage(){
		
		return "SeekerView/MyPage";
	}
	
}
