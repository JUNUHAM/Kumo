package net.kumo.kumo.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.kumo.kumo.domain.dto.AdminDashboardDTO;
import net.kumo.kumo.domain.dto.JobSummaryDTO;
import net.kumo.kumo.domain.dto.ReportDTO;
import net.kumo.kumo.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    // 대시보드 페이지
    @GetMapping("/dashboard")
    public String dashboardPage(Model model,
                                @RequestParam(value = "lang", defaultValue = "ko") String lang) {
        model.addAttribute("adminName", "Administrator");
        model.addAttribute("lang", lang);
        return "adminView/admin_dashboard";
    }

    // 대시보드 데이터 (JSON)
    @GetMapping("/data")
    @ResponseBody
    public AdminDashboardDTO getDashboardData() {
        return adminService.getDashboardData();
    }

    /**
     * [VIEW] 공고 관리 페이지
     * URL: /admin/post
     */
    @GetMapping("/post")
    public String postManagementPage(Model model,
                                     @RequestParam(value = "lang", defaultValue = "ko") String lang) {
        model.addAttribute("adminName", "Administrator");
        model.addAttribute("lang", lang); // 뷰에서 언어 판단용

        // 서비스에 lang 전달 -> DTO 생성 시 제목/직무 등을 해당 언어로 변환
        List<JobSummaryDTO> posts = adminService.getAllJobSummaries(lang);
        model.addAttribute("posts", posts);

        List<ReportDTO> reports = adminService.getAllReports(lang);
        model.addAttribute("reports", reports);

        return "adminView/admin_post";
    }

    /**
     * 공고 삭제 API
     * URL: /admin/post/delete
     */
    @PostMapping("/post/delete")
    @ResponseBody
    public ResponseEntity<String> deletePosts(@RequestBody Map<String, List<String>> payload) {
        List<String> mixedIds = payload.get("ids");
        log.info("삭제 요청 공고 목록: {}", mixedIds);

        adminService.deleteMixedPosts(mixedIds);

        return ResponseEntity.ok("Deleted successfully");
    }

    /**
     * 신고 내역 삭제 API
     * URL: /admin/report/delete
     */
    @PostMapping("/report/delete")
    @ResponseBody
    public ResponseEntity<String> deleteReports(@RequestBody Map<String, List<Long>> payload) {
        List<Long> ids = payload.get("ids");
        log.info("삭제 요청 신고 목록: {}", ids);

        adminService.deleteReports(ids);

        return ResponseEntity.ok("Deleted successfully");
    }
}