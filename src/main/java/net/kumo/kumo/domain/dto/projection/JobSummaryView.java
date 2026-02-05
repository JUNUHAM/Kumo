package net.kumo.kumo.domain.dto.projection;

public interface JobSummaryView {

    Long getId();
    String getImgUrls(); // 이미지 원본

    // --- 🇰🇷 한국어 원본 데이터 ---
    String getTitle();
    String getCompanyName();
    String getAddress(); // 공통 주소
    String getContactPhone();
    String getWage();
    String getWriteTime();

    // --- 🇯🇵 일본어 원본 데이터 ---
    // 리포지토리가 이 값들을 DB에서 퍼와야 하므로 반드시 있어야 합니다!
    String getTitleJp();
    String getCompanyNameJp();
    String getWageJp();
    // String getAddressJp(); // 필요하다면 추가 (엔티티에 필드가 있어야 함)

    // --- 좌표 ---
    Double getLat();
    Double getLng();

    // 썸네일 자르는 것 정도는 유틸리티 성격이라 여기에 둬도 괜찮
    default String getThumbnailUrl() {
        String urls = getImgUrls();
        if (urls == null || urls.isBlank()) {
            return null;
        }
        return urls.split(",")[0].trim();
    }

    // ★ 삭제: default String getLocalizedTitle(...)
    // 이유:JobResponse DTO 생성자에서 처리하기 때문입니다.
}