package net.kumo.kumo.domain.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class JobPostingRequestDTO {
    private Long id; // 🌟 수정 시 필요한 기본 키
    private Long datanum; // 🌟 공고 고유 번호
    private String title; // 제목
    private String position; // 직책
    private String jobDescription;// 🌟 [업무 상세] 이름 통일
    private String contactPhone; // 연락처
    private String body; // 🌟 [상세정보] 이름 통일
    private String salaryType; // HOURLY, DAILY 등
    private Integer salaryAmount; // 금액
    private Long companyId; // 회사 ID
}