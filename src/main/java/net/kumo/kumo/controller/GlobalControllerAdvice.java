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
                // 1. 유저 객체 그대로 전달
                model.addAttribute("user", user);

                // 2. [fullName 가공] 엔티티 필드를 직접 합칩니다.
                String fullName = (user.getNameKanjiSei() != null ? user.getNameKanjiSei() : "")
                        + " "
                        + (user.getNameKanjiMei() != null ? user.getNameKanjiMei() : "");
                model.addAttribute("fullName", fullName.trim());

                // 3. [age 가공] birthDate가 있다면 현재 날짜 기준으로 계산합니다.
                if (user.getBirthDate() != null) {
                    int age = Period.between(user.getBirthDate(), LocalDate.now()).getYears();
                    model.addAttribute("age", age);
                } else {
                    model.addAttribute("age", 0);
                }
            }
        } catch (Exception e) {
            System.out.println("Global Data Error: " + e.getMessage());
        }
    }
}