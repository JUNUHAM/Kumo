package net.kumo.kumo.controller;

import lombok.RequiredArgsConstructor;
import net.kumo.kumo.domain.dto.projection.JobSummaryView;
import net.kumo.kumo.service.MapService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping("/map")
@RequiredArgsConstructor
public class MapController {

    private final MapService mapService;

    /**
     * [VIEW] 구인 리스트 페이지 반환
     * 파일 위치: resources/templates/mapView/job_list.html
     */
    @GetMapping("/job-list-view")
    public String jobListPage() {
        // ★ 핵심: templates 폴더 아래의 경로를 정확히 적어줍니다.
        // .html 확장자는 생략합니다.
        return "mapView/job_list";
    }

    // [API] 데이터 반환 (기존 유지)
    @GetMapping("/api/jobs")
    @ResponseBody
    public List<JobSummaryView> getJobListApi(
            @RequestParam Double minLat,
            @RequestParam Double maxLat,
            @RequestParam Double minLng,
            @RequestParam Double maxLng
    ) {
        return mapService.getJobListInMap(minLat, maxLat, minLng, maxLng);
    }
}