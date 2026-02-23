package net.kumo.kumo.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.kumo.kumo.domain.dto.SeekerMyPageDTO;
import net.kumo.kumo.service.SeekerService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
