package net.kumo.kumo.controller;

import java.security.Principal;
import java.time.LocalDate;
import java.time.Period;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value; // 🌟 추가
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import net.kumo.kumo.domain.entity.UserEntity;
import net.kumo.kumo.service.RecruiterService;

@ControllerAdvice
public class GlobalControllerAdvice {

    @Autowired
    private RecruiterService rs;

    // 🌟 [추가 1] application.properties에 있는 구글맵 키를 가져옵니다!
    // (괄호 안의 이름은 사장님 properties 파일에 적힌 이름과 똑같이 맞춰주세요)
    @Value("${GOOGLE_MAPS_KEY}") // 대문자와 언더바까지 환경변수랑 똑같이 맞춰줍니다!
    private String googleMapsKey;

    @ModelAttribute
    public void addAttributes(Model model, Principal principal) {

        // 🌟 [추가 2] 로그인 여부와 상관없이 모든 화면에 구글맵 키를 배달합니다!
        model.addAttribute("googleMapsKey", googleMapsKey);

        if (principal == null)
            return;

        try {
            String email = principal.getName();

            // 🔥 (참고) 사장님의 이 getCurrentUser() 메서드가 DB에서 매번 최신 정보를
            // 새로 꺼내오고 있다면, 정보 수정 후에도 알아서 최신 정보로 갱신될 겁니다!
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

                // 4. 생년월일 가공
                if (user.getBirthDate() != null) {
                    String birthStr = user.getBirthDate().toString().replace("-", ""); // 1990-01-01 -> 19900101
                    if (birthStr.length() >= 8) {
                        model.addAttribute("birthYear", birthStr.substring(0, 4));
                        model.addAttribute("birthMonth", birthStr.substring(4, 6));
                        model.addAttribute("birthDay", birthStr.substring(6, 8));
                    }
                } else {
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