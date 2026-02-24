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

				// 4. 생년월일 미리 잘라서 보내기
				if (user.getBirthDate() != null) {
					String birthStr = user.getBirthDate().toString().replace("-", "");
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

				// ★★★ [이번에 추가할 핵심 로직!] 프로필 이미지 URL 전용 속성 쏘기 ★★★
				if (user.getProfileImage() != null) {
					// 유저가 올린 사진이 있으면 그 URL을 쏜다!
					model.addAttribute("profileImageUrl", user.getProfileImage().getFileUrl());
				} else {
					// 사진이 없으면 명시적으로 null을 쏜다!
					model.addAttribute("profileImageUrl", null);
				}
			}
		} catch (Exception e) {
			System.out.println("Global Data Error: " + e.getMessage());
		}
	}
}