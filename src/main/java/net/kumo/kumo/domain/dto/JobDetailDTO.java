package net.kumo.kumo.domain.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import net.kumo.kumo.domain.entity.*;

@Getter
@NoArgsConstructor
public class JobDetailDTO {
    private Long id;
    private String source;         // ★ 데이터 출처 (OSAKA, TOKYO 등)
    private String title;
    private String companyName;
    private String address;
    private String wage;
    private String contactPhone;

    private String position;       // 업무
    private String jobDescription; // 업무 상세
    private String body;           // 상세 정보 (본문)

    private String imgUrls;        // 이미지
    private Double lat;            // 지도용 위도
    private Double lng;            // 지도용 경도

    // ★ [수정] 생성자: source 파라미터 추가
    public JobDetailDTO(BaseEntity entity, String lang, String source) {
        this.id = entity.getId();
        this.source = source;      // ★ 소스 저장
        this.contactPhone = entity.getContactPhone();
        this.imgUrls = entity.getImgUrls();
        this.address = entity.getAddress();

        boolean isJp = "jp".equalsIgnoreCase(lang);

        // 1. 언어별 데이터 매핑 (일본어 설정이고 데이터가 있으면 일본어, 아니면 기본값)
        this.title = (isJp && hasText(entity.getTitleJp())) ? entity.getTitleJp() : entity.getTitle();
        this.companyName = (isJp && hasText(entity.getCompanyNameJp())) ? entity.getCompanyNameJp() : entity.getCompanyName();
        this.wage = (isJp && hasText(entity.getWageJp())) ? entity.getWageJp() : entity.getWage();
        this.position = (isJp && hasText(entity.getPositionJp())) ? entity.getPositionJp() : entity.getPosition();
        this.jobDescription = (isJp && hasText(entity.getJobDescriptionJp())) ? entity.getJobDescriptionJp() : entity.getJobDescription();

        // 상세 내용 (body가 없으면 notes라도 보여주기)
        String bodyRaw = entity.getBody();
        if (bodyRaw == null || bodyRaw.isBlank()) {
            // 일본어 notes 확인
            String notes = (isJp && hasText(entity.getNotesJp())) ? entity.getNotesJp() : entity.getNotes();
            if (notes != null) {
                bodyRaw = notes;
            }
        }
        this.body = bodyRaw;

        // 2. 좌표 데이터 추출 (Geocoded 엔티티인 경우에만)
        if (entity instanceof OsakaGeocodedEntity) {
            this.lat = ((OsakaGeocodedEntity) entity).getLat();
            this.lng = ((OsakaGeocodedEntity) entity).getLng();
        } else if (entity instanceof TokyoGeocodedEntity) {
            this.lat = ((TokyoGeocodedEntity) entity).getLat();
            this.lng = ((TokyoGeocodedEntity) entity).getLng();
        } else {
            // NoGeocoded 테이블은 좌표 없음
            this.lat = null;
            this.lng = null;
        }
    }

    // 헬퍼 메소드: 문자열이 비어있지 않은지 확인
    private boolean hasText(String str) {
        return str != null && !str.isBlank();
    }
}