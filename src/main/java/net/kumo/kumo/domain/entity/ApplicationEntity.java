package net.kumo.kumo.domain.entity;


import jakarta.persistence.*;
import lombok.*;
import net.kumo.kumo.domain.entity.Enum.ApplicationStatus;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;


@Entity
@Table(name = "applications")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ApplicationEntity {
	
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "app_id")
	private Long id;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "job_post_id", nullable = false)
	private JobPostingEntity jobPosting;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "seeker_id", nullable = false)
	private UserEntity seeker;
	
	@Enumerated(EnumType.STRING)
	@Column(length = 20)
	private ApplicationStatus status;
	
	@CreationTimestamp
	@Column(name = "applied_at", updatable = false)
	private LocalDateTime appliedAt;
}
