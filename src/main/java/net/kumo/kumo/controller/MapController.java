package net.kumo.kumo.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.kumo.kumo.domain.dto.JobDetailDTO;
import net.kumo.kumo.domain.dto.JobSummaryDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import net.kumo.kumo.service.MapService;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

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


	/**
	 * 지도 메인페이지 연결
	 * @return 메인 페이지
	 */
	@GetMapping("/main")
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

            Model model
    ) {
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
}