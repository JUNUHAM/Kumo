package net.kumo.kumo.domain.dto.projection;

public interface JobSummaryView {

    // 1. 식별자
    Long getId();

    // ==========================================
    // 🇰🇷 한국어 데이터 (기존)
    // ==========================================
    String getTitle();          // title
    String getCompanyName();    // company_name
    String getAddress();        // address
    String getContactPhone();   // contact_phone
    String getWage();           // wage (급여 정보도 추가 추천)

    // ==========================================
    // 🇯🇵 일본어 데이터 (추가됨)
    // BaseEntity의 필드명과 정확히 일치해야 함
    // ==========================================
    String getTitleJp();        // title_jp
    String getCompanyNameJp();  // company_name_jp
    String getWageJp();         // wage_jp
    // 주소의 경우 Entity 구조상 addressJp라는 단일 필드는 없고
    // prefectureJp, cityJp 등으로 나뉘어 있어, 필요하다면 아래처럼 추가 가능합니다.
    // 단, NoGeocoded 테이블에는 해당 컬럼이 없으므로 @Formula 처리가 안되어 있다면 에러가 날 수 있습니다.
    // 우선은 안전하게 Title, Company, Wage만 추가합니다.

    // 3. 이미지
    String getImgUrls();

    // 4. 좌표
    Double getLat();
    Double getLng();

    // ==========================================
    // 🛠️ 유틸리티 메소드 (Default Method)
    // ==========================================

    /**
     * 썸네일 URL 가져오기
     */
    default String getThumbnailUrl() {
        String urls = getImgUrls();
        if (urls == null || urls.isBlank()) {
            return null; // 프론트에서 기본 이미지 처리
        }
        return urls.split(",")[0].trim();
    }

    /**
     * [스마트 Getter] 언어 코드에 따라 알맞은 제목 반환
     * @param lang "jp"면 일본어, 그 외엔 한국어
     */
    default String getLocalizedTitle(String lang) {
        if ("jp".equalsIgnoreCase(lang) && getTitleJp() != null && !getTitleJp().isBlank()) {
            return getTitleJp();
        }
        return getTitle();
    }

    /**
     * [스마트 Getter] 언어 코드에 따라 알맞은 회사명 반환
     */
    default String getLocalizedCompanyName(String lang) {
        if ("jp".equalsIgnoreCase(lang) && getCompanyNameJp() != null && !getCompanyNameJp().isBlank()) {
            return getCompanyNameJp();
        }
        return getCompanyName();
    }

    /**
     * [스마트 Getter] 언어 코드에 따라 알맞은 급여 반환
     */
    default String getLocalizedWage(String lang) {
        if ("jp".equalsIgnoreCase(lang) && getWageJp() != null && !getWageJp().isBlank()) {
            return getWageJp();
        }
        return getWage();
    }
}