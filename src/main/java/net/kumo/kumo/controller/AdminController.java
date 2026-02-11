package net.kumo.kumo.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.kumo.kumo.domain.dto.AdminDashboardDTO;
import net.kumo.kumo.domain.dto.ReportDTO;
import net.kumo.kumo.domain.entity.JobPostingEntity;
import net.kumo.kumo.repository.JobPostingRepository;
import net.kumo.kumo.repository.ReportRepository;
import net.kumo.kumo.service.AdminService;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequestMapping("admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final JobPostingRepository jobPostingRepo;
    private final ReportRepository reportRepo;

    /**
     * 1. 관리자 대시보드 화면으로 이동
     * URL: http://localhost:8080/admin/dashboard
     */
    @GetMapping("/dashboard")
    public String dashboardPage(Model model) {
        // {User} 부분에 넣을 관리자 이름 (나중에 세션에서 가져오기)
        model.addAttribute("adminName", "Administrator");
        return "adminView/admin_dashboard"; // templates/admin/dashboard.html
    }

    /**
     * 2. 대시보드 통계 데이터 제공 (JSON)
     * URL: http://localhost:8080/admin/data
     * 프론트엔드 fetch() 함수가 호출하는 곳
     */
    @GetMapping("/data")
    @ResponseBody // HTML이 아니라 JSON 데이터를 반환한다는 표시
    public AdminDashboardDTO getDashboardData() {
        return adminService.getDashboardData();
    }

    /**
     * [VIEW] 공고 관리 페이지 (전체 공고 + 신고 관리)
     * URL: /admin/post
     */
    @GetMapping("/post")
    public String postManagementPage(Model model) {
        model.addAttribute("adminName", "Administrator");

        // 탭 1 데이터: 전체 공고
        List<JobPostingEntity> posts = adminService.getAllJobPostings();
        model.addAttribute("posts", posts);

        // 탭 2 데이터: 신고 내역
        List<ReportDTO> reports = adminService.getAllReports();
        model.addAttribute("reports", reports);

        return "adminView/admin_post";
    }
}