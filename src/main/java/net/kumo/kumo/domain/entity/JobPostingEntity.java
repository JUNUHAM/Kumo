package net.kumo.kumo.domain.entity;


import jakarta.persistence.*;
import lombok.*;
import net.kumo.kumo.domain.entity.Enum.JobStatus;
import net.kumo.kumo.domain.entity.Enum.SalaryType;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "job_postings")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class JobPostingEntity {
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "job_post_id")
	private Long id;
	
	// 작성자(구인자)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private UserEntity recruiter;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "region_id")
	private RegionEntity region;
	
	@Column(nullable = false, length = 200)
	private String title;
	
	@Column(length = 100)
	private String position;
	
	@Column(columnDefinition = "TEXT")
	private String description;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "salary_type", length = 20)
	private SalaryType salaryType;
	
	@Column(name = "salary_amount")
	private Integer salaryAmount;
	
	@Column(name = "work_address", length = 255)
	private String workAddress;
	
	private Double latitude;
	private Double longitude;
	
	@Enumerated(EnumType.STRING)
	@Column(length = 20)
	private JobStatus status;
	
	private LocalDate deadline;
	
	@Column(name = "view_count")
	private Integer viewCount;
	
	@CreationTimestamp
	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;
}