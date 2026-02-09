package net.kumo.kumo.domain.entity;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "scraps")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ScrapEntity {
	
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "scrap_id")
	private Long id;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private UserEntity user;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "job_post_id", nullable = false)
	private JobPostingEntity jobPosting;
	
	@CreationTimestamp
	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;
}
