package net.kumo.kumo.domain.dto;

import java.time.LocalDate;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class JobPostFormDTO {
    private String title;
    private String position;
    private String positionDetail; // DB엔 없지만 폼에 있어서 받음
    private LocalDate deadline; // "YYYY-MM-DD"가 자동으로 변환되어 들어옴
    private String salaryType; // HOURLY, DAILY, MONTHLY, SALARY
    private Integer salaryAmount;
    private String description;
    private Long companyId; // 등록 회사 ID

    // 파일 업로드를 받을 리스트
    private List<MultipartFile> images;
}