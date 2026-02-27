package net.kumo.kumo.controller; // 패키지명 확인! (본인 프로젝트에 맞게)

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.kumo.kumo.domain.dto.ChangeNewPWDTO;
import net.kumo.kumo.domain.dto.JoinRecruiterDTO;
import net.kumo.kumo.domain.dto.JoinSeekerDTO;
import net.kumo.kumo.service.LoginService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Controller
public class LoginController {
	private final LoginService LoginService;
	
	@Value("${file.upload.dir}")
	private String uploadDir;
	
	@Value("${kumo.google.maps.keys}")
	private String googleMapsKey;
	
	@GetMapping("login")
	public String login() {
		return "NonLoginView/login";
	}
	
	@GetMapping("join")
	public String join() {
		return "NonLoginView/join";
	}
	
	@GetMapping("FindId")
	public String FindId() {
		return "NonLoginView/FindId";
	}
	
	@GetMapping("FindPw")
	public String FindPw() {
		return "NonLoginView/FindPw";
	}
	
	@GetMapping("/join/seeker")
	public String JoinSeeker(Model model) {
		model.addAttribute("googleKey", googleMapsKey);
		
		return "NonLoginView/joinSeeker";
		
	}
	
	@PostMapping("/join/seeker")
	public String joinSeeker(@ModelAttribute JoinSeekerDTO dto) {
		log.info("dto 받아온거 :{}", dto);
		LoginService.insertSeeker(dto);
		
		return "redirect:/login";
	}
	
	@GetMapping("/join/recruiter")
	public String JoinRecruiter(Model model) {
		model.addAttribute("googleKey", googleMapsKey);
		
		return "NonLoginView/joinRecruiter";
	}
	
	@PostMapping("/join/recruiter")
	public String joinRecruiterProcess(@ModelAttribute JoinRecruiterDTO dto) {
		log.info("dto 받아온거 :{}", dto);
		
		// 1. 저장된 파일명들을 담을 리스트 생성
		List<String> savedFileNames = new ArrayList<>();
		
		try {
			log.info(">>> 구인자 가입 요청: {}", dto.getEmail());
			log.info(">>> 파일 저장 경로: {}", uploadDir);
			
			// 2. 폴더가 없으면 생성 (안전장치)
			File dir = new File(uploadDir);
			if (!dir.exists()) {
				boolean created = dir.mkdirs();
				if (created)
					log.info(">>> 업로드 폴더 생성 완료");
			}
			
			// 3. 파일 저장 반복문
			List<MultipartFile> files = dto.getEvidenceFiles(); // DTO에서 파일 리스트 가져오기
			if (files != null) {
				for (MultipartFile file : files) {
					// 빈 파일은 패스
					if (file.isEmpty())
						continue;
					
					// 3-1. 파일명 중복 방지 (UUID 사용)
					String originalFilename = file.getOriginalFilename();
					String savedFilename = UUID.randomUUID() + "_" + originalFilename;
					
					// 3-2. 전체 경로 생성 (설정파일 경로 + 새 파일명)
					// uploadDir 끝에 '/'가 없으면 붙여주는 로직 (안전장치)
					String fullPath = uploadDir.endsWith("/") ? uploadDir + savedFilename
							: uploadDir + "/" + savedFilename;
					
					// 3-3. 실제 파일 저장
					file.transferTo(new File(fullPath));
					
					// 3-4. 저장 성공한 파일명을 리스트에 추가
					savedFileNames.add(savedFilename);
					log.info(">>> 파일 저장 완료: {}", savedFilename);
				}
			}
			
			// 4. 서비스 호출 (DTO + 파일명 리스트 전달)
			// 서비스에서 DB에 회원정보와 파일명들을 insert 해야 함
			LoginService.joinRecruiter(dto, savedFileNames);
			
			// 5. 성공 시 로그인 페이지로 이동
			return "redirect:/join/wait";
			
		} catch (IOException e) {
			log.error(">>> 파일 업로드 중 에러 발생", e);
			// 에러 발생 시 가입 페이지로 돌려보냄 (에러 파라미터 전달)
			return "redirect:/join/recruiter?error=upload";
		} catch (Exception e) {
			log.error(">>> 회원가입 처리 중 에러 발생", e);
			return "redirect:/join/recruiter?error=fail";
		}
	}
	
	@GetMapping("/join/wait")
	public String joinWait() {
		return "NonLoginView/joinWait";
	}
	
	@PostMapping("/changePw")
	public String changePw(@RequestParam("email") String email, Model model) {
		model.addAttribute("email", email);
		return "NonLoginView/changePw";
	}
	
	@PostMapping("ChangeNewPW")
	public String NewPw(@RequestParam ChangeNewPWDTO ChangeNewPWDTO) {
		log.info("새로받은 비밀번호 {}", ChangeNewPWDTO);
		
		LoginService.ChangeNewPW(ChangeNewPWDTO);
		
		return "redirect:/login";
	}
	
}