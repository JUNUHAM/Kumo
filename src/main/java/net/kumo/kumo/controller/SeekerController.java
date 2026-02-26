package net.kumo.kumo.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.kumo.kumo.domain.dto.JoinSeekerDTO;
import net.kumo.kumo.domain.dto.SeekerMyPageDTO;
import net.kumo.kumo.service.SeekerService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@RequestMapping("Seeker")
@Slf4j
@RequiredArgsConstructor
@Controller
public class SeekerController {
    private final SeekerService seekerService;

    @GetMapping("/MyPage")
    public String SeekerMyPage(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        SeekerMyPageDTO dto = seekerService.getDTO(userDetails.getUsername());
        model.addAttribute("user", dto);

        return "SeekerView/MyPage";
    }
	
	@GetMapping("/ProfileEdit")
	public String SeekerProfileEdit(Model model){
		return "SeekerView/SeekerProfileEdit";
	}
	
	@PostMapping("/ProfileEdit")
	public String SeekerPrfileEdit(@ModelAttribute JoinSeekerDTO dto){
		
		log.info("dto, 잘들어옴? : {}",dto);
		
		seekerService.updateProfile(dto);
		return "redirect:/Seeker/MyPage";
		
	}
	

}
