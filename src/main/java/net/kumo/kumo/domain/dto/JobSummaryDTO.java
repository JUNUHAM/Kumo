package net.kumo.kumo.domain.dto; // projection 패키지 아님

import lombok.Getter;
import net.kumo.kumo.domain.dto.projection.JobSummaryView;

@Getter
public class JobSummaryDTO {

    // JobSummaryView 에서 모두 처리하는 대신 DTO를 생성해서 더 간결하게 변경
    
    private Long id;
    private String title;        // 언어에 따라 바뀐 제목이 여기에 들어감
    private String companyName;  // 언어에 따라 바뀐 회사명이 여기에 들어감
    private String address;
    private String wage;         // 언어에 따라 바뀐 급여
    private String contactPhone;
    private String thumbnailUrl;
    private String writeTime;
    private Double lat;
    private Double lng;

    // 생성자: 원본 데이터(view)와 언어(lang)를 받아서 알아서 정리함
    public JobSummaryDTO(JobSummaryView view, String lang) {
        this.id = view.getId();
        this.thumbnailUrl = view.getThumbnailUrl(); // 인터페이스의 default 메소드 활용
        this.contactPhone = view.getContactPhone();
        this.address = view.getAddress();
        this.writeTime = view.getWriteTime(); // 필요하다면 포맷팅도 여기서 가능
        this.lat = view.getLat();
        this.lng = view.getLng();

        // ★ 핵심 로직: 언어에 따라 하나만 선택해서 저장
        boolean isJp = "jp".equalsIgnoreCase(lang);

        // 1. 제목
        this.title = (isJp && hasText(view.getTitleJp())) ? view.getTitleJp() : view.getTitle();

        // 2. 회사명
        this.companyName = (isJp && hasText(view.getCompanyNameJp())) ? view.getCompanyNameJp() : view.getCompanyName();

        // 3. 급여
        this.wage = (isJp && hasText(view.getWageJp())) ? view.getWageJp() : view.getWage();
    }

    // 문자열이 비어있는지 확인
    private boolean hasText(String str) {
        return str != null && !str.isBlank();
    }
}