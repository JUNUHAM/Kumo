package net.kumo.kumo.domain.entity;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Immutable;

import java.time.LocalDateTime;

@Entity
@Table(name = "tokyo_geocoded")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Immutable
public class TokyoGeocodedEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "row_no")
	private Long rowNo;
	
	private String datanum;
	private String title;
	private String href;
	
	@Column(name = "write_time")
	private String writeTime;
	
	@Column(name = "img_urls", columnDefinition = "TEXT")
	private String imgUrls;
	
	@Column(columnDefinition = "TEXT")
	private String body;
	
	@Column(name = "company_name")
	private String companyName;
	
	private String address;
	
	@Column(name = "contact_phone")
	private String contactPhone;
	
	private String position;
	
	@Column(name = "job_description", columnDefinition = "TEXT")
	private String jobDescription;
	
	private String wage;
	
	@Column(columnDefinition = "TEXT")
	private String notes;
	
	@Column(name = "title_jp")
	private String titleJp;
	
	@Column(name = "company_name_jp")
	private String companyNameJp;
	
	@Column(name = "position_jp")
	private String positionJp;
	
	@Column(name = "job_description_jp", columnDefinition = "TEXT")
	private String jobDescriptionJp;
	
	@Column(name = "wage_jp")
	private String wageJp;
	
	@Column(name = "notes_jp", columnDefinition = "TEXT")
	private String notesJp;
	
	private Double lat;
	private Double lng;
	
	@Column(name = "prefecture_jp")
	private String prefectureJp;
	
	@Column(name = "ward_city_jp")
	private String wardCityJp;
	
	@Column(name = "prefecture_kr")
	private String prefectureKr;
	
	@Column(name = "ward_city_kr")
	private String wardCityKr;
	
	@CreationTimestamp
	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;
}
