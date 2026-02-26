package net.kumo.kumo.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import net.kumo.kumo.domain.enums.JobStatus;

@Entity
@Table(name = "tokyo_geocoded")
@Getter
@Setter
public class TokyoGeocodedEntity extends BaseEntity { // BaseEntity 와의 상속관계 설정 (중복 제거 및 코드 통합)

	// [추가해야 할 필수 필드들]
	private Long datanum;
	private Integer rowNo;
	private String title;
	private String body;
	private String wage;
	private String href;
	private String position;
	// ... 오사카에 있는 필드들을 쫙 복사해서 넣어주세요!

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private UserEntity user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "company_id")
	private CompanyEntity company;

	@Enumerated(EnumType.STRING)
	private JobStatus status;

	// 도쿄 특화 필드 (테이블 구조에 맞게)

	@Column(nullable = false)
	private Double lat;

	@Column(nullable = false)
	private Double lng;

	@Column(name = "prefecture_jp")
	private String prefectureJp;

	@Column(name = "ward_city_jp") // 도쿄는 city/ward 통합
	private String wardCityJp;

	@Column(name = "prefecture_kr")
	private String prefectureKr;

	@Column(name = "ward_city_kr")
	private String wardCityKr;

	// 수정 시 필요한 급여정보
	@Column(name = "salary_type")
	private String salaryType;

	@Column(name = "salary_amount")
	private Integer salaryAmount;
}