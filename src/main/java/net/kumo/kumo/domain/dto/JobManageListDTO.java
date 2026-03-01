package net.kumo.kumo.domain.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
@Builder
public class JobManageListDTO {
    private Long id;
    private Long datanum;
    private String title;
    private String position; // 🌟 추가! (이게 있어야 메인에 직무가 나옴)
    private String regionType;
    private String wage;
    private String wageJp;
    private String contactPhone; // 🌟 추가! (이게 있어야 메인에 연락처가 나옴)
    private LocalDateTime createdAt;
    private String status;
}