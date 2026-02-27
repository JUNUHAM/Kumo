package net.kumo.kumo.domain.dto;

import lombok.Data;

@Data
public class ScheduleDTO {
    private Long scheduleId; // 🌟 딱 이거 한 줄만 있으면 됩니다! (있으면 통과!)
    private String title;
    private String description;
    private String start; // JS에서 "2026-02-19T14:00" 형태로 보냄
    private String end;
    private String color;
}