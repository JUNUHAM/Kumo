package net.kumo.kumo.controller;

import java.io.File;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.kumo.kumo.domain.dto.JobPostFormDTO;
import net.kumo.kumo.domain.dto.JoinRecruiterDTO;
import net.kumo.kumo.domain.entity.CompanyEntity;
import net.kumo.kumo.domain.entity.UserEntity;
import net.kumo.kumo.repository.UserRepository;
import net.kumo.kumo.service.JobPostingService;
import net.kumo.kumo.service.RecruiterService;

// 구인자 페이지 컨트롤러
@Slf4j
@RequiredArgsConstructor
@RequestMapping("Recruiter")
@Controller
public class RecruiterController {

    private final UserRepository ur;
    private final RecruiterService rs;
    private final JobPostingService js;

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
     * 공고 등록 컨트롤러!
     * 
     * @param model
     * @return
     */
    // 예시: RecruiterController.java 내부
    @GetMapping("/JobPosting")
    public String showJobPostForm(Model model, Principal principal) {
        // 1. 로그인한 사장님의 이메일(또는 ID)로 유저 정보를 찾습니다.
        String userEmail = principal.getName();
        UserEntity user = ur.findByEmail(userEmail).orElseThrow();

        // 2. 사장님이 등록해둔 회사 목록을 가져옵니다.
        // (UserEntity 안에 List<CompanyEntity> companies 가 있다고 가정)
        List<CompanyEntity> myCompanies = user.getCompanies();

        // 3. 화면(HTML)으로 회사 목록을 넘겨줍니다! 이름은 "companies"로 합니다.
        model.addAttribute("companies", myCompanies);

        return "recruiterView/jobPosting"; // HTML 파일명
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

    /**
     * 회원정보 수정 컨트롤러
     * 
     * @param model
     * @return
     */
    @GetMapping("/ProfileEdit") // 습관적으로 앞에 슬래시(/)를 붙여주시면 라우팅 꼬임을 방지할 수 있습니다.
    public String ProfileEdit(Model model) {
    
		return "recruiterView/profileEdit";
    }

    /**
     * 회원정보 수정 요청
     * 
     * @return
     */
    @PostMapping("/ProfileEdit")
    public String ProfileEdit(@ModelAttribute JoinRecruiterDTO dto) {

        // TODO: rs.updateProfile(...) 같은 서비스 로직을 호출해서 DB를 수정합니다.
        log.info("회원정보 수정 요청 들어옴!");

        log.info("dto 받아온거 :{}", dto);
        rs.updateProfile(dto);

        // 수정이 완료되면 다시 설정 페이지나 메인 화면으로 돌려보냅니다. (새로고침 방지용 redirect 필수!)
        return "redirect:/Recruiter/Settings";
    }

    // 🌟 [추가] 폼에서 날아온 데이터를 DB에 저장하는 POST 요청
    @PostMapping("/JobPosting")
    public String registerJobPost(@ModelAttribute JobPostFormDTO formDTO, Principal principal) {

        // 1. 현재 로그인한 사장님 이메일(ID) 가져오기
        String userEmail = principal.getName();

        // 2. 서비스로 데이터 넘겨서 DB에 저장하기
        js.saveJobPost(formDTO, userEmail);

        // 3. 저장이 끝나면 어디로 갈지? (예: 공고 목록 페이지나 메인으로 이동)
        return "redirect:/Recruiter/Main";
    }

    // ----------------------------------------------------------------
    // ------------------------- 회사정보 구역 --------------------------
    // ----------------------------------------------------------------

}