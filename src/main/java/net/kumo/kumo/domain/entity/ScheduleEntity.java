package net.kumo.kumo.domain.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "schedules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleEntity {
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "schedule_id")
	private Long id;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "recruiter_id", nullable = false)
	private UserEntity recruiter;
	
	@Column(length = 200)
	private String title;
	
	@Column(name = "start_datetime")
	private LocalDateTime startDatetime;
	
	@Column(name = "end_datetime")
	private LocalDateTime endDatetime;
	
	@Column(name = "schedule_detail", columnDefinition = "TEXT")
	private String scheduleDetail;
	
	@Column(name = "color_code", length = 20)
	private String colorCode;
}
