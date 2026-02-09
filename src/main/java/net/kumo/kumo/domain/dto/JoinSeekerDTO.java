package net.kumo.kumo.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JoinSeekerDTO {
	
	/* --- 1. 성명 정보 --- */
	private String nameKanjiSei; // HTML: name_kanji_sei
	private String nameKanjiMei; // HTML: name_kanji_mei
	private String nameKanaSei;  // HTML: name_kana_sei
	private String nameKanaMei;  // HTML: name_kana_mei
	
	/* --- 2. 계정 정보 --- */
	private String nickname;
	private String email;
	private String password;
	
	/* --- 3. 개인 신상 (생년월일 조합용) --- */
	private Integer birthYear;
	private Integer birthMonth;
	private Integer birthDay;
	
	private String gender; // "M" or "F"
	private String contact; // 전화번호
	
	/* --- 4. 주소 정보 (보여주기용) --- */
	private String zipCode;       // HTML: zip_code
	private String addressMain;   // HTML: address_main
	private String addressDetail; // HTML: address_detail
	
	/* --- 5. [추가] 주소 상세 정보 (Hidden Fields) --- */
	private String addrPrefecture; // 도/현
	private String addrCity;       // 시/구
	private String addrTown;       // 동/읍
	
	/* --- 6. [추가] 지도 좌표 (Hidden Fields) --- */
	private Double latitude;
	private Double longitude;
	
	/* --- 7. 가입 정보 --- */
	private String joinPath;
	private boolean adReceive; // 체크박스
	
	/**
	 * [편의 메서드]
	 * 쪼개진 년/월/일을 LocalDate로 합쳐서 반환
	 * Service나 Entity 변환 시 사용
	 */
	public LocalDate getBirthDate() {
		if (birthYear == null || birthMonth == null || birthDay == null) {
			return null;
		}
		try {
			return LocalDate.of(birthYear, birthMonth, birthDay);
		} catch (Exception e) {
			return null;
		}
	}
}