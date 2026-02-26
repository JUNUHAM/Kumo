package net.kumo.kumo.domain.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JobPostingRequestDTO {
    private Long datanum; // 🌟 [추가] 이게 없어서 에러가 났던 겁니다!
    private String title; // 제목
    private String position; // 직책
    private String jobDescription; // 🌟 업무 상세 (이름 통일)
    private String contactPhone; // 연락처
    private String body; // 🌟 상세정보 (이름 통일)
    private String salaryType; // 급여 타입
    private Integer salaryAmount; // 급여 금액
    private Long companyId; // 선택된 회사 ID
}