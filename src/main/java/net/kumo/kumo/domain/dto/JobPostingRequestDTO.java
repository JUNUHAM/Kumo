package net.kumo.kumo.domain.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JobPostingRequestDTO {

    private String title; // 제목
    private String position; // 직책
    private String positionDetail; // 업무 상세 → jobDescription
    private String contactPhone; // 연락처
    private String description; // 상세정보 → body
    private String salaryType; // 급여 타입
    private Integer salaryAmount; // 급여 금액
    private Long companyId; // 선택된 회사 ID
}
