package net.kumo.kumo.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List; // ★ List 임포트 필수

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JoinRecruiterDTO {
	
	/* --- 1. 성명 정보 (대표자 또는 담당자) --- */
	private String nameKanjiSei; // HTML: nameKanjiSei
	private String nameKanjiMei; // HTML: nameKanjiMei
	private String nameKanaSei;  // HTML: nameKanaSei
	private String nameKanaMei;  // HTML: nameKanaMei
	
	/* --- 2. 계정 정보 --- */
	private String nickname;     // 회사명 또는 담당자 별명
	private String email;
	private String password;
	
	/* --- 3. 담당자 생년월일 (조합용) --- */
	private Integer birthYear;
	private Integer birthMonth;
	private Integer birthDay;
	
	private String gender; // "M" or "F"
	private String contact; // 전화번호 (회사 또는 담당자)
	
	/* --- 4. 주소 정보 (보여주기용) --- */
	private String zipCode;       // 우편번호
	private String addressMain;   // 기본 주소
	private String addressDetail; // 상세 주소
	
	/* --- 5. 주소 상세 정보 (Hidden Fields - DB 저장용) --- */
	private String addrPrefecture; // 도/현 (Tokyo)
	private String addrCity;       // 시/구 (Shinjuku)
	private String addrTown;       // 동/읍 (Nishishinjuku)
	
	/* --- 6. 지도 좌표 (Hidden Fields) --- */
	private Double latitude;
	private Double longitude;
	
	/* --- 7. 가입 정보 --- */
	private String joinPath;   // 가입 경로
	private boolean adReceive; // 마케팅 수신 동의
	
	/* --- ★★★ 8. 증빙서류 (다중 파일 업로드) ★★★ --- */
	// HTML에서 <input type="file" name="evidenceFiles" multiple> 로 보낸 파일들을 받음
	private List<MultipartFile> evidenceFiles;
	
	/**
	 * [편의 메서드]
	 * 쪼개진 년/월/일을 LocalDate로 합쳐서 반환
	 * Service에서 Entity로 변환할 때 사용
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