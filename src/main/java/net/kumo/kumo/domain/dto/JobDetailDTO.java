package net.kumo.kumo.domain.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import net.kumo.kumo.domain.entity.*;

@Getter
@NoArgsConstructor
public class JobDetailDTO {
	private Long id;
	private String source;         // 출처 (OSAKA, TOKYO 등)
	private String title;
	private String companyName;
	private String address;
	private String wage;
	private String contactPhone;
	
	private String position;       // 업무
	private String jobDescription; // 업무 상세 요약
	private String body;           // 상세 정보 (본문 전체)
	
	private String imgUrls;        // 이미지
	private Double lat;            // 지도용 위도
	private Double lng;            // 지도용 경도
	
	/**
	 * 엔티티와 언어 설정(lang)을 받아 DTO를 생성합니다.
	 * @param entity DB에서 조회한 엔티티
	 * @param lang 언어 코드 ("ja": 일본어, "ko": 한국어)
	 * @param source 데이터 출처 ("OSAKA", "TOKYO")
	 */
	public JobDetailDTO(BaseEntity entity, String lang, String source) {
		// 1. 공통 데이터 매핑 (언어 무관)
		this.id = entity.getId();
		this.source = source;
		this.contactPhone = entity.getContactPhone();
		this.imgUrls = entity.getImgUrls();
		this.address = entity.getAddress(); // 주소는 기본 컬럼 사용 (필요 시 주소도 분기 가능)
		
		// 2. 언어 감지 ("ja"인 경우에만 true)
		boolean isJp = "ja".equalsIgnoreCase(lang);
		
		// 3. 언어별 데이터 매핑 (Helper 메소드 resolveText 사용)
		// 일본어 설정(isJp)이고, 일본어 데이터가 있으면 -> 일본어 반환
		// 그 외(한국어 설정이거나, 일본어 데이터가 없으면) -> 한국어(기본값) 반환
		this.title = resolveText(isJp, entity.getTitleJp(), entity.getTitle());
		this.companyName = resolveText(isJp, entity.getCompanyNameJp(), entity.getCompanyName());
		this.wage = resolveText(isJp, entity.getWageJp(), entity.getWage());
		this.position = resolveText(isJp, entity.getPositionJp(), entity.getPosition());
		
		// 4. 상세 내용 (Body) 처리 로직
		// 우선순위: JobDescription(상세) > Notes(비고) > Body(원문)
		String desc = resolveText(isJp, entity.getJobDescriptionJp(), entity.getJobDescription());
		String notes = resolveText(isJp, entity.getNotesJp(), entity.getNotes());
		
		if (hasText(desc)) {
			this.body = desc;
		} else if (hasText(notes)) {
			this.body = notes;
		} else {
			this.body = entity.getBody(); // 최후의 수단 (HTML 원문 등)
		}
		
		// 화면 표기용 jobDescription 필드에도 동일하게 할당
		this.jobDescription = this.body;
		
		// 5. 좌표 데이터 추출 (Geocoded 엔티티인 경우에만)
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
	
	/**
	 * 언어 설정과 데이터 존재 여부에 따라 적절한 텍스트를 반환합니다.
	 */
	private String resolveText(boolean isJp, String jpText, String krText) {
		if (isJp && hasText(jpText)) {
			return jpText;
		}
		return krText;
	}
	
	/**
	 * 문자열이 null이 아니고 공백이 아닌지 확인합니다.
	 */
	private boolean hasText(String str) {
		return str != null && !str.isBlank();
	}
}