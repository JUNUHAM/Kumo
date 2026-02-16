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
@RequestMapping("map")
@RequiredArgsConstructor
public class MapController {

	// Key
	@Value("${GOOGLE_MAPS_KEY}")
	private String googleMapKey;
	
    private final MapService mapService;

    // --- 화면 반환 (View) ---

	/**
	 * 지도 메인페이지 연결
	 * @return 메인 페이지
	 */
	@GetMapping("main")
	public String mainMap(Model model) {
		log.debug("메인화면 연결");

		model.addAttribute("googleMapsKey", googleMapKey);
		return "mainView/main";
	}
    /**
     * [VIEW] 구인 리스트 페이지 반환
     * 파일 위치: resources/templates/mapView/job_list.html
     */
    @GetMapping("/job-list-view")
    public String jobListPage() {
        return "mapView/job_list";
    }

    /**
     * 공고 상세 페이지 이동
     * @param id        공고 아이디
     * @param source    지역 꼬리표 'OSAKA', 'TOKYO' 등
     * @param lang      언어 설정 'kr', 'jp'
     * @param isOwner   공고 작성자 여부 (임시 테스트용, 추후 로그인 기능 구현시 Authenticate 로 변경)
     * @param model
     * @return          mapView/job_detail.html 로 이동
     */
    @GetMapping("/jobs/detail")
    public String jobDetailPage(
            @RequestParam Long id,
            @RequestParam String source,
            @RequestParam(defaultValue = "kr") String lang,
            // ★ [테스트용] URL 뒤에 &isOwner=true 를 붙이면 구인자 모드로 전환
            // 기본값은 false (구직자 모드)
            @RequestParam(defaultValue = "false") boolean isOwner,

            Model model) {
        // 1. 서비스에서 상세 데이터 조회
        JobDetailDTO job = mapService.getJobDetail(id, source, lang);

        // 2. 모델에 담기
        model.addAttribute("job", job);
        model.addAttribute("googleMapsKey", googleMapKey); // 지도 표시용

        // 로그인 로직 대신, 파라미터로 유저 여부 전달
        model.addAttribute("isOwner", isOwner);

        model.addAttribute("lang", lang);

        // 4. 뷰 반환 (templates/mapView/job_detail.html)
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
        // 서비스가 이미 정제된(JobResponse) 데이터를 줍니다.
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