package net.kumo.kumo.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "users") // DB 테이블명
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEntity {
	
	/* ==========================
	   1. 계정 식별 정보
	   ========================== */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "user_id")
	private Long userId;
	
	@Column(nullable = false, unique = true, length = 100)
	private String email;
	
	@Column(length = 255)
	private String password;
	
	@Column(length = 50)
	private String nickname;
	
	// DB: ENUM('SEEKER', 'RECRUITER', 'ADMIN') DEFAULT 'SEEKER'
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Enum.UserRole role;
	
	
	/* ==========================
	   2. 성명 정보 (4개 필드)
	   ========================== */
	@Column(name = "name_kanji_sei", nullable = false, length = 50)
	private String nameKanjiSei;
	
	@Column(name = "name_kanji_mei", nullable = false, length = 50)
	private String nameKanjiMei;
	
	@Column(name = "name_kana_sei", nullable = false, length = 50)
	private String nameKanaSei;
	
	@Column(name = "name_kana_mei", nullable = false, length = 50)
	private String nameKanaMei;
	
	
	/* ==========================
	   3. 개인 신상 정보
	   ========================== */
	@Column(name = "birth_date")
	private LocalDate birthDate;
	
	// DB: ENUM('MALE', 'FEMALE', 'OTHER')
	@Enumerated(EnumType.STRING)
	@Column(length = 10)
	private Enum.Gender gender;
	
	@Column(length = 20, unique = true)
	private String contact;
	
	@Column(name = "profile_image", length = 255)
	@Builder.Default
	private String profileImage = "/images/default_profile.png";
	
	
	/* ==========================
	   4. 주소 정보 (표시용)
	   ========================== */
	@Column(name = "zip_code", length = 10)
	private String zipCode;
	
	@Column(name = "address_main", length = 255)
	private String addressMain;
	
	@Column(name = "address_detail", length = 255)
	private String addressDetail;
	
	
	/* ==========================
	   5. 주소 정보 (검색용 3단 분리)
	   ========================== */
	@Column(name = "addr_prefecture", length = 50)
	private String addrPrefecture; // 도/현
	
	@Column(name = "addr_city", length = 50)
	private String addrCity;       // 시/구
	
	@Column(name = "addr_town", length = 50)
	private String addrTown;       // 동/읍
	
	
	/* ==========================
	   6. 위치 정보 (지도용 좌표)
	   ========================== */
	// DB: DECIMAL(10, 8) -> Java: Double
	@Column(columnDefinition = "DECIMAL(10, 8)")
	private Double latitude;
	
	@Column(columnDefinition = "DECIMAL(11, 8)")
	private Double longitude;
	
	/* ==========================
	   7. 가입 및 메타 정보
	   ========================== */
	@Column(name = "join_path", length = 50)
	private String joinPath;
	
	@Column(name = "ad_receive")
	@Builder.Default
	private boolean adReceive = false;
	
	@Column(name = "is_active")
	@Builder.Default
	private boolean isActive = true;
	
	// 소셜 로그인용 (nullable)
	@Column(name = "social_provider", length = 20)
	private String socialProvider;
	
	@Column(name = "social_id", length = 100)
	private String socialId;
	
	
	/* ==========================
	   8. 타임스탬프 (자동 관리)
	   ========================== */
	@CreationTimestamp
	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;
	
	@UpdateTimestamp
	@Column(name = "updated_at")
	private LocalDateTime updatedAt;
	
	
	// ... 기존 필드들 (id, email, password 등) ...
	
	// 1. 실패 횟수 저장 (기본값 0)
	@Column(name = "login_fail_count", nullable = false)
	private int loginFailCount = 0;
	
	// 2. 마지막 실패 시간 (null 가능)
	@Column(name = "last_fail_at")
	private LocalDateTime lastFailAt;
	
	// 로그인 실패 시 호출: 횟수 +1, 시간 갱신
	public void increaseFailCount() {
		this.loginFailCount++;
		this.lastFailAt = LocalDateTime.now();
	}
	
	// 로그인 성공 시 호출: 횟수 0으로 초기화
	public void resetFailCount() {
		this.loginFailCount = 0;
		this.lastFailAt = null;}
	
}