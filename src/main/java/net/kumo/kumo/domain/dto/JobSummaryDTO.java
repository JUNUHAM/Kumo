package net.kumo.kumo.domain.dto; // projection 패키지 아님

import lombok.Getter;
import net.kumo.kumo.domain.dto.projection.JobSummaryView;

@Getter
public class JobSummaryDTO {

    // JobSummaryView 에서 모두 처리하는 대신 DTO를 생성해서 더 간결하게 변경
    
    private Long id;
    private String source; // 지역 구분용 꼬리표 (OSAKA, TOKYO 등)
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
    // 지역 구분용 꼬리표(source) 추가
    public JobSummaryDTO(JobSummaryView view, String lang, String source) {
        this.id = view.getId();
        this.source = source; // 꼬리표 저장

        this.thumbnailUrl = view.getThumbnailUrl();
        this.contactPhone = view.getContactPhone();
        this.address = view.getAddress();
        this.writeTime = view.getWriteTime();
        
        // 좌표 처리
        this.lat = view.getLat();
        this.lng = view.getLng();

        // 언어 설정 처리
        boolean isJp = "ja".equalsIgnoreCase(lang);
        this.title = (isJp && view.getTitleJp() != null) ? view.getTitleJp() : view.getTitle();
        this.companyName = (isJp && view.getCompanyNameJp() != null) ? view.getCompanyNameJp() : view.getCompanyName();
        this.wage = (isJp && view.getWageJp() != null) ? view.getWageJp() : view.getWage();
    }

    // 문자열이 비어있는지 확인
    private boolean hasText(String str) {
        return str != null && !str.isBlank();
    }
}