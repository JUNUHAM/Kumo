	package net.kumo.kumo.controller;
	
	import lombok.RequiredArgsConstructor;
	import lombok.extern.slf4j.Slf4j;
	import net.kumo.kumo.domain.dto.JobDetailDTO;
	import net.kumo.kumo.domain.dto.JobSummaryDTO;
	import net.kumo.kumo.service.MapService;
	import org.springframework.beans.factory.annotation.Value;
	import org.springframework.stereotype.Controller;
	import org.springframework.ui.Model;
	import org.springframework.web.bind.annotation.GetMapping;
	import org.springframework.web.bind.annotation.RequestMapping;
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
		 * 직업 상세 페이지로 이동
		 * @param id
		 * @param source
		 * @param lang
		 * @param model
		 * @return
		 */
	    @GetMapping("/jobs/detail")
	    public String jobDetailPage(
	            @RequestParam Long id,
	            @RequestParam String source,
	            @RequestParam(defaultValue = "kr") String lang,
	            Model model
	    ) {
	        // 1. 서비스에서 상세 데이터 조회
	        JobDetailDTO job = mapService.getJobDetail(id, source, lang);
	
	        // 2. 모델에 담기
	        model.addAttribute("job", job);
	        model.addAttribute("googleMapsKey", googleMapKey); // 지도 표시용
	
	        // 3. 뷰 반환 (templates/mapView/job_detail.html)
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