package net.kumo.kumo.domain.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.kumo.kumo.domain.enums.JobStatus;

@Entity
@Table(name = "osaka_geocoded", indexes = {
		@Index(name = "idx_lat_lng", columnList = "lat, lng"),
		@Index(name = "idx_company_address", columnList = "company_name, address"),
		@Index(name = "idx_region_jp", columnList = "prefecture_jp, city_jp, ward_jp"),
		@Index(name = "idx_region_kr", columnList = "prefecture_kr, city_kr, ward_kr")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OsakaGeocodedEntity extends BaseEntity {

	// 1. 핵심 식별자 및 순번 (CSV: row_no, datanum)
	@Column(name = "row_no")
	private Integer rowNo;

	@Column(name = "datanum", unique = true) // 🌟 uk_datanum 반영
	private Long datanum;

	// 2. 기본 정보 (CSV: title, href, write_time)
	@Column(name = "title", length = 200)
	private String title;

	@Column(name = "href", length = 500)
	private String href;

	@Column(name = "write_time")
	private String writeTime; // CSV의 write_time 필드

	// 3. 사진 및 본문 (CSV: img_urls, body)
	@Column(name = "img_urls", length = 1000)
	private String imgUrls;

	@Lob
	@Column(name = "body")
	private String body;

	// 4. 회사 및 연락처 (CSV: company_name, address, contact_phone)
	@Column(name = "company_name", length = 255)
	private String companyName;

	@Column(name = "address", length = 500)
	private String address;

	@Column(name = "contact_phone", length = 255)
	private String contactPhone;

	// 5. 직무 상세 (CSV: position, job_description, wage, notes)
	@Column(name = "position", length = 100)
	private String position;

	@Column(name = "job_description", columnDefinition = "TEXT")
	private String jobDescription;

	@Column(name = "wage")
	private String wage;

	@Column(name = "notes", columnDefinition = "TEXT")
	private String notes;

	// 6. 일본어 번역 필드 (CSV 반영: title_jp ~ notes_jp)
	@Column(name = "title_jp", length = 200)
	private String titleJp;

	@Column(name = "company_name_jp", length = 255)
	private String companyNameJp;

	@Column(name = "position_jp", length = 100)
	private String positionJp;

	@Column(name = "job_description_jp", columnDefinition = "TEXT")
	private String jobDescriptionJp;

	@Column(name = "wage_jp")
	private String wageJp;

	@Column(name = "notes_jp", columnDefinition = "TEXT")
	private String notesJp;

	// 7. GIS 정보 (CSV: lat, lng)
	@Column(name = "lat", nullable = false)
	private Double lat;

	@Column(name = "lng", nullable = false)
	private Double lng;

	// 8. 지역 정보 (CSV: prefecture_jp ~ ward_kr)
	@Column(name = "prefecture_jp")
	private String prefectureJp;

	@Column(name = "city_jp")
	private String cityJp;

	@Column(name = "ward_jp")
	private String wardJp;

	@Column(name = "prefecture_kr")
	private String prefectureKr;

	@Column(name = "city_kr")
	private String cityKr;

	@Column(name = "ward_kr")
	private String wardKr;

	// 9. 리크루터 시스템 전용 필드 (새로 등록하는 공고용)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private UserEntity user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "company_id")
	private CompanyEntity company;

	@Enumerated(EnumType.STRING)
	@Column(columnDefinition = "ENUM('RECRUITING', 'CLOSED') DEFAULT 'RECRUITING'")
	private JobStatus status;

	@CreationTimestamp
	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt; // 🌟 SET created_at = NOW() 와 매핑

	@PrePersist
	public void prePersist() {
		if (this.status == null)
			this.status = JobStatus.RECRUITING;
	}

	// 수정 시 필요한 급여정보
	@Column(name = "salary_type")
	private String salaryType;

	@Column(name = "salary_amount")
	private Integer salaryAmount;
}