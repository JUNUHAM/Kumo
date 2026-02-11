package net.kumo.kumo.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.kumo.kumo.domain.dto.JobDetailDTO;
import net.kumo.kumo.domain.dto.JobSummaryDTO;
import net.kumo.kumo.domain.dto.ReportDTO;
import net.kumo.kumo.domain.entity.UserEntity; // 실제 User 엔티티 경로 확인 필요
import net.kumo.kumo.service.MapService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("map") // 기본 경로
@RequiredArgsConstructor
public class MapController {

    @Value("${GOOGLE_MAPS_KEY}")
    private String googleMapKey;

    private final MapService mapService;

    // --- 화면 반환 (View) ---

    @GetMapping("main")
    public String mainMap(Model model) {
        log.debug("메인화면 연결");
        model.addAttribute("googleMapsKey", googleMapKey);
        return "mainView/main";
    }

    @GetMapping("/job-list-view")
    public String jobListPage() {
        return "mapView/job_list";
    }

    @GetMapping("/jobs/detail")
    public String jobDetailPage(
            @RequestParam Long id,
            @RequestParam String source,
            @RequestParam(defaultValue = "kr") String lang,
            @RequestParam(defaultValue = "false") boolean isOwner,
            Model model
    ) {
        JobDetailDTO job = mapService.getJobDetail(id, source, lang);

        model.addAttribute("job", job);
        model.addAttribute("googleMapsKey", googleMapKey);
        model.addAttribute("isOwner", isOwner);
        model.addAttribute("lang", lang);

        return "mapView/job_detail";
    }

    // --- 데이터 반환 (API) ---

    @GetMapping("/api/jobs")
    @ResponseBody
    public List<JobSummaryDTO> getJobListApi(
            @RequestParam Double minLat,
            @RequestParam Double maxLat,
            @RequestParam Double minLng,
            @RequestParam Double maxLng,
            @RequestParam(defaultValue = "kr") String lang
    ) {
        return mapService.getJobListInMap(minLat, maxLat, minLng, maxLng, lang);
    }

    /**
     * [NEW] 신고 접수 API
     * URL: /map/api/reports (주의: 클래스 매핑 "map" + 메소드 매핑 "/api/reports")
     * 프론트엔드 fetch 주소 수정 필요: fetch('/map/api/reports', ...)
     */
    @PostMapping("/api/reports")
    @ResponseBody
    public ResponseEntity<String> submitReport(@RequestBody ReportDTO reportDTO, HttpSession session) {

        // 1. 로그인 체크 (세션에 "loginUser"라는 이름으로 유저 객체가 있다고 가정)
        // 실제 프로젝트의 세션 키값 확인 필요
        Object sessionUser = session.getAttribute("loginUser");

        if (sessionUser == null) {
            // 401 Unauthorized 반환 -> 프론트에서 로그인 페이지로 이동 처리
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        // 2. 신고자 ID 설정 (User 엔티티 캐스팅)
        if (sessionUser instanceof UserEntity) {
            UserEntity user = (UserEntity) sessionUser;
            reportDTO.setReporterId(user.getUserId()); // User 엔티티의 ID Getter 사용
        }

        // 3. 서비스 호출
        mapService.createReport(reportDTO);

        return ResponseEntity.ok("신고가 정상적으로 접수되었습니다.");
    }
}