package net.kumo.kumo.controller;

import java.util.List; // 🌟 1. List import 추가

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.RequiredArgsConstructor;
import net.kumo.kumo.domain.entity.CompanyEntity;
import net.kumo.kumo.domain.entity.UserEntity;
import net.kumo.kumo.security.AuthenticatedUser; // 패키지 경로 확인!
import net.kumo.kumo.service.CompanyService;
import net.kumo.kumo.service.RecruiterService;

@Controller
@RequestMapping("/Recruiter")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;
    private final RecruiterService recruiterService;

    @Value("${kumo.google.maps.keys}") // 🌟 2. properties와 맞춰서 's' 제거 확인
    private String googleMapsKey;

    /**
     * 회사 정보 관리 메인 (조회 및 신규 등록 폼 통합)
     */
    @GetMapping("/CompanyInfo")
    public String companyInfo(@RequestParam(value = "id", required = false) Long id,
            Model model, @AuthenticationPrincipal AuthenticatedUser authenticatedUser) { // 🌟 3. 콤마(,) 제거 완료

        // 🌟 4. 시큐리티 유저가 없으면 로그인으로 리다이렉트
        if (authenticatedUser == null) {
            return "redirect:/login";
        }

        // 🌟 5. 이미 recruiterService에서 현재 유저를 잘 가져오고 있다면 그대로 사용
        UserEntity loginUser = recruiterService.getCurrentUser(authenticatedUser.getUsername());

        List<CompanyEntity> companyList = companyService.getCompanyList(loginUser);
        CompanyEntity currentCompany;

        model.addAttribute("currentMenu", "companyInfo");

        if (id == null) {
            currentCompany = new CompanyEntity();
            model.addAttribute("isNew", true);
        } else {
            currentCompany = companyService.getCompany(id);
            model.addAttribute("isNew", false);
        }

        model.addAttribute("companyList", companyList);
        model.addAttribute("currentCompany", currentCompany);
        model.addAttribute("googleMapsKey", googleMapsKey);

        return "recruiterView/companyInfo";
    }

    /**
     * 수정 및 저장 프로세스
     */
    @PostMapping("/CompanyUpdate")
    public String updateCompany(@ModelAttribute CompanyEntity company,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) { // 🌟 6. 세션 대신 시큐리티 사용

        if (authenticatedUser == null) {
            return "redirect:/login";
        }

        // 세션에서 꺼내지 말고 서비스의 getCurrentUser를 사용하세요!
        UserEntity loginUser = recruiterService.getCurrentUser(authenticatedUser.getUsername());

        companyService.saveCompany(company, loginUser);

        return "redirect:/Recruiter/CompanyInfo?id=" + company.getCompanyId();
    }

    /**
     * 삭제 프로세스
     */
    @GetMapping("/CompanyDelete")
    public String deleteCompany(@RequestParam("id") Long id,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        if (authenticatedUser == null)
            return "redirect:/login";

        companyService.deleteCompany(id);
        return "redirect:/Recruiter/CompanyInfo";
    }

    @GetMapping("/CompanyAdd")
    public String companyAddForm() {
        return "redirect:/Recruiter/CompanyInfo";
    }
}