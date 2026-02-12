package net.kumo.kumo.controller;

import java.io.File;
import java.security.Principal;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.kumo.kumo.service.RecruiterService;

// 구인자 페이지 컨트롤러
@Slf4j
@RequiredArgsConstructor
@RequestMapping("Recruiter")
@Controller
public class RecruiterController {

    private final RecruiterService rs;

    /**
     * 홈 메뉴 컨트롤러
     * 
     * @param model
     * @return
     */
    @GetMapping("Main")
    public String Main(Model model) {
        model.addAttribute("currentMenu", "home"); // 사이드바 선택(홈 메뉴)
        return "recruiterView/main";
    }

    /**
     * 지원자 관리 컨트롤러
     * 
     * @param model
     * @return
     */
    @GetMapping("ApplicantInfo")
    public String ApplicantInfo(Model model) {
        model.addAttribute("currentMenu", "applicants"); // 사이드바 선택((지원자 관리)
        return "recruiterView/applicantInfo";
    }

    /**
     * 공고 관리 컨트롤러
     * 
     * @param model
     * @return
     */
    @GetMapping("JobManage")
    public String JobManage(Model model) {
        model.addAttribute("currentMenu", "jobManage"); // 사이드바 선택(공고 관리)
        return "recruiterView/jobManage";
    }

    /**
     * 캘린더 컨트롤러
     * 
     * @param model
     * @return
     */
    @GetMapping("Calendar")
    public String Calender(Model model) {
        model.addAttribute("currentMenu", "calendar"); // 사이드바 선택(캘린더)
        return "recruiterView/calendar";
    }

    /**
     * 회사 정보 컨트롤러
     * 
     * @param model
     * @return
     */
    @GetMapping("CompanyInfo")
    public String CompanyInfo(Model model) {
        model.addAttribute("currentMenu", "companyInfo"); // 사이드바 선택(회사 정보))
        return "recruiterView/companyInfo";
    }

    /**
     * 내 계정(settings) 컨트롤러
     * 
     * @param model
     * @return
     */
    @GetMapping("/Settings")
    public String Settings(Model model, Principal principal) {
        model.addAttribute("currentMenu", "settings"); // 사이드바 선택(내 계정))
        return "recruiterView/settings";
    }

    /**
     * 설정 프로필 사진 업로드
     * 
     * @param file
     * @param principal
     * @return
     */
    @PostMapping("/UploadProfile")
    @ResponseBody
    public ResponseEntity<?> uploadProfile(@RequestParam("profileImage") MultipartFile file, Principal principal) {
        try {
            if (file.isEmpty())
                return ResponseEntity.badRequest().body("파일이 없습니다.");

            // [핵심 수정] 맥북의 사용자 홈 디렉토리(/Users/이름)를 기준으로 경로를 잡습니다.
            // 이렇게 하면 톰캣 임시 폴더와 섞이지 않습니다.
            String uploadDir = System.getProperty("user.home") + "/kumo_uploads/profiles/";

            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs(); // 폴더가 없으면 생성 (매우 중요!)
            }

            // 파일명 생성
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();

            // [중요] 절대 경로를 사용해 새 파일 객체를 만듭니다.
            File dest = new File(uploadDir + fileName);

            // 파일 저장
            file.transferTo(dest);

            // DB에는 웹에서 접근 가능한 가상 경로를 저장합니다.
            String userEmail = principal.getName();
            String webPath = "/upload/profiles/" + fileName;
            rs.updateProfileImage(userEmail, webPath);

            return ResponseEntity.ok().body(Map.of("success", true, "imageUrl", webPath));
        } catch (Exception e) {
            e.printStackTrace(); // 콘솔에 상세 에러 출력
            return ResponseEntity.status(500).body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * 공고 등록 컨트롤러
     * 
     * @param model
     * @return
     */
    @GetMapping("JobPosting")
    public String JobPosting(Model model) {
        return "recruiterView/jobPosting";
    }

    /**
     * 지원자 상세보기 컨트롤러
     * 
     * @param model
     * @return
     */
    @GetMapping("ApplicantDetail")
    public String ApplicantDetail(Model model) {
        return "recruiterView/applicantDetail";
    }
}