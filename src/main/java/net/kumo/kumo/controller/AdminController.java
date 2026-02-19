package net.kumo.kumo.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.kumo.kumo.domain.dto.AdminDashboardDTO;
import net.kumo.kumo.domain.dto.JobSummaryDTO;
import net.kumo.kumo.domain.dto.ReportDTO;
import net.kumo.kumo.domain.dto.UserManageDTO;
import net.kumo.kumo.service.AdminService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    @GetMapping("/user")
    public String userManagementPage(Model model,
                                     @RequestParam(value = "lang", defaultValue = "ko") String lang,
                                     @RequestParam(value = "searchType", required = false) String searchType,
                                     @RequestParam(value = "keyword", required = false) String keyword,
                                     @RequestParam(value = "role", required = false) String role,
                                     @RequestParam(value = "status", required = false) String status,
                                     @RequestParam(value = "page", defaultValue = "0") int page,
                                     @RequestParam(value = "size", defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<UserManageDTO> users = adminService.getAllUsers(lang, searchType, keyword, role, status, pageable);

        model.addAttribute("users", users);
        model.addAttribute("lang", lang);

        // 필터값 유지
        model.addAttribute("searchType", searchType);
        model.addAttribute("keyword", keyword);
        model.addAttribute("role", role);
        model.addAttribute("status", status);

        // --- 상단 통계 데이터 계산 (전체 데이터 기준) ---
        // 실제로는 DB 쿼리(countBy...)로 가져오는 게 성능상 좋지만, 여기선 users 객체나 전체 리스트 활용 가정
        long totalCount = users.getTotalElements(); // 필터링 된 개수지만, 통계용으론 전체 DB 카운트가 맞음 (서비스에 별도 메소드 권장)

        // --- 페이지네이션 로직 ---
        int totalPages = users.getTotalPages();
        if (totalPages == 0) totalPages = 1;
        int pageBlock = 5;
        int current = users.getNumber() + 1;
        int startPage = Math.max(1, current - (pageBlock / 2));
        int endPage = Math.min(totalPages, startPage + pageBlock - 1);

        if (endPage - startPage + 1 < pageBlock && totalPages >= pageBlock) {
            startPage = endPage - pageBlock + 1;
        }

        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("totalPages", totalPages);

        return "adminView/admin_user";
    }

    @GetMapping("/user/stats")
    public ResponseEntity<Map<String, Object>> getUserStats() {
        // 서비스에서 Map을 받아와서 그대로 JSON으로 리턴
        Map<String, Object> stats = adminService.getUserStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * [VIEW] 공고 관리 페이지
     * URL: /admin/post
     */
    @GetMapping("/post")
    public String postManagementPage(Model model,
                                     @RequestParam(value = "lang", defaultValue = "ko") String lang,
                                     @RequestParam(value = "searchType", required = false) String searchType,
                                     @RequestParam(value = "keyword", required = false) String keyword,
                                     @RequestParam(value = "status", required = false) String status,
                                     // [추가] 페이지 번호 (0부터 시작), 페이지당 개수 (기본 10개)
                                     @RequestParam(value = "page", defaultValue = "0") int page,
                                     @RequestParam(value = "size", defaultValue = "10") int size) {

        // 1. Pageable 객체 생성 (최신순 정렬은 Service에서 처리하므로 여기선 정보만 전달)
        Pageable pageable = PageRequest.of(page, size);

        // 2. Service 반환 타입을 List -> Page로 변경
        Page<JobSummaryDTO> posts = adminService.getAllJobSummaries(lang, searchType, keyword, status, pageable);

        model.addAttribute("posts", posts);

        // 필터 유지용 파라미터
        model.addAttribute("lang", lang);
        model.addAttribute("searchType", searchType);
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);

        int totalPages = posts.getTotalPages();
        if (totalPages == 0) totalPages = 1; // 0페이지 방지

        int pageBlock = 5; // 보여줄 버튼 개수 (5개로 설정)
        int current = posts.getNumber() + 1; // 0-based -> 1-based

        // 현재 페이지를 기준으로 시작 페이지 계산 (중앙 정렬 느낌)
        int startPage = Math.max(1, current - (pageBlock / 2));
        int endPage = Math.min(totalPages, startPage + pageBlock - 1);

        // [보정] 마지막 페이지가 5개보다 적을 때, 앞쪽 페이지를 더 보여줘서 5개를 채움
        if (endPage - startPage + 1 < pageBlock && totalPages >= pageBlock) {
            startPage = endPage - pageBlock + 1;
        }

        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("totalPages", totalPages); // 맨 끝 버튼용

        // 신고 목록 (기존 유지)
        model.addAttribute("reports", adminService.getAllReports(lang));

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