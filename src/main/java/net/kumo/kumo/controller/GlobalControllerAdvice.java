package net.kumo.kumo.controller;

import java.security.Principal;
import java.time.LocalDate;
import java.time.Period;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import net.kumo.kumo.domain.entity.UserEntity;
import net.kumo.kumo.service.RecruiterService;

@ControllerAdvice
public class GlobalControllerAdvice {

    @Autowired
    private RecruiterService rs;

    @ModelAttribute
    public void addAttributes(Model model, Principal principal) {
        if (principal == null)
            return;

        try {
            String email = principal.getName();
            UserEntity user = rs.getCurrentUser(email);

            if (user != null) {
                // 1. 유저 객체 전달
                model.addAttribute("user", user);

                // 2. fullName 가공
                String fullName = (user.getNameKanjiSei() != null ? user.getNameKanjiSei() : "")
                        + " "
                        + (user.getNameKanjiMei() != null ? user.getNameKanjiMei() : "");
                model.addAttribute("fullName", fullName.trim());

                // 3. age 가공
                if (user.getBirthDate() != null) {
                    int age = Period.between(user.getBirthDate(), LocalDate.now()).getYears();
                    model.addAttribute("age", age);
                } else {
                    model.addAttribute("age", 0);
                }

                // ★★★ [추가된 부분] 생년월일 미리 잘라서 보내기 (에러 해결 핵심!) ★★★
                if (user.getBirthDate() != null) {
                    String birthStr = user.getBirthDate().toString().replace("-", ""); // 1990-01-01 -> 19900101
                    if (birthStr.length() >= 8) {
                        model.addAttribute("birthYear", birthStr.substring(0, 4));
                        model.addAttribute("birthMonth", birthStr.substring(4, 6));
                        model.addAttribute("birthDay", birthStr.substring(6, 8));
                    }
                } else {
                    // 없으면 빈 문자열 보냄 (HTML에서 에러 안 나게)
                    model.addAttribute("birthYear", "");
                    model.addAttribute("birthMonth", "");
                    model.addAttribute("birthDay", "");
                }
            }
        } catch (Exception e) {
            System.out.println("Global Data Error: " + e.getMessage());
        }
    }
}