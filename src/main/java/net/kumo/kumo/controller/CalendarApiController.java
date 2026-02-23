package net.kumo.kumo.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import net.kumo.kumo.domain.dto.ScheduleDTO;
import net.kumo.kumo.domain.entity.ScheduleEntity;
import net.kumo.kumo.security.AuthenticatedUser;
import net.kumo.kumo.service.ScheduleService;

@RestController
@RequestMapping("/api/calendar")
@RequiredArgsConstructor
public class CalendarApiController {

    private final ScheduleService scheduleService;

    /**
     * 일정 저장
     * 
     * @param dto
     * @param user
     * @return
     */
    @PostMapping("/save")
    public ResponseEntity<?> saveEvent(@RequestBody ScheduleDTO dto,
            @AuthenticationPrincipal AuthenticatedUser user) {
        ScheduleEntity entity = new ScheduleEntity();
        entity.setTitle(dto.getTitle());
        entity.setDescription(dto.getDescription());
        entity.setStartAt(LocalDateTime.parse(dto.getStart()));
        entity.setEndAt(LocalDateTime.parse(dto.getEnd()));
        entity.setColorCode(dto.getColor());

        scheduleService.saveSchedule(entity, user.getUsername());
        return ResponseEntity.ok().build();
    }

    /**
     * 스케줄 가져오기
     * 
     * @param user
     * @return
     */
    @GetMapping("/events")
    public List<Map<String, Object>> getEvents(@AuthenticationPrincipal AuthenticatedUser user) {
        // 현재 로그인한 리쿠르터의 일정만 가져오기
        return scheduleService.getCalendarEvents(user.getUsername());
    }

    /**
     * 일정 삭제
     * 
     * @param id
     * @param user
     * @return
     */
    // 쓰레기통으로 드래그 앤 드롭할 때 여기로 도착합니다!
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteCalendarEvent(@PathVariable("id") Long id) {
        try {
            // 1. 사장님이 만드신 서비스 메서드로 ID 전달 -> DB에서 삭제 완료!
            scheduleService.deleteSchedule(id);

            // 2. 삭제가 무사히 끝나면 화면(프론트엔드)에 "성공" 도장을 찍어 보냅니다.
            return ResponseEntity.ok("일정이 성공적으로 삭제되었습니다.");

        } catch (Exception e) {
            // 혹시라도 삭제 중 에러가 나면 500 에러와 함께 이유를 알려줍니다.
            return ResponseEntity.status(500).body("삭제 실패: " + e.getMessage());
        }
    }

    /**
     * 일정 수정 (모달창 수정 & 드래그 이동 시 여기로 옵니다!)
     * * @param dto
     * 
     * @param user
     * @return
     */
    @PostMapping("/update")
    public ResponseEntity<?> updateEvent(@RequestBody ScheduleDTO dto,
            @AuthenticationPrincipal AuthenticatedUser user) {

        try {
            // 🌟 1. 기존 일정 수정이므로, DTO에서 ID를 받아와야 합니다.
            // (주의: ScheduleDTO에 id 필드(Long id)가 있어야 합니다!)
            ScheduleEntity entity = new ScheduleEntity();
            entity.setScheduleId(dto.getScheduleId()); // 기존 일정을 덮어쓰기 위해 ID 필수!
            entity.setTitle(dto.getTitle());
            entity.setDescription(dto.getDescription());
            entity.setStartAt(LocalDateTime.parse(dto.getStart()));
            entity.setEndAt(LocalDateTime.parse(dto.getEnd()));
            entity.setColorCode(dto.getColor());

            // 🌟 2. 서비스로 넘겨서 DB 업데이트 (JPA의 save는 ID가 있으면 update로 작동합니다)
            scheduleService.saveSchedule(entity, user.getUsername());

            return ResponseEntity.ok().build();

        } catch (Exception e) {
            return ResponseEntity.status(500).body("수정 실패: " + e.getMessage());
        }
    }

}