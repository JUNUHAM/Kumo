package net.kumo.kumo.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "seeker_careers")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class SeekerCareerEntity {
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "career_id")
	private Long id;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private UserEntity user;
	
	@Column(name = "company_name", length = 100)
	private String companyName;
	
	@Column(length = 100)
	private String department;
	
	@Column(name = "start_date")
	private LocalDate startDate;
	
	@Column(name = "end_date")
	private LocalDate endDate;
	
	@Column(columnDefinition = "TEXT")
	private String description;
}