package net.kumo.kumo.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
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

}