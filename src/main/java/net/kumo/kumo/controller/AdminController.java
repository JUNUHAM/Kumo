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
    public String dashboardPage(Model model) {
        model.addAttribute("adminName", "Administrator");
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
    public String postManagementPage(Model model) {
        model.addAttribute("adminName", "Administrator");

        // 탭 1: 전체 공고 (JobSummaryDTO)
        // Service에서 이미 4개 테이블 데이터를 합쳐서 줍니다.
        List<JobSummaryDTO> posts = adminService.getAllJobSummaries();
        model.addAttribute("posts", posts);

        // 탭 2: 신고 내역
        List<ReportDTO> reports = adminService.getAllReports();
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