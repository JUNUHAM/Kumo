package net.kumo.kumo.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import net.kumo.kumo.domain.entity.Enum.ReportStatus;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;


@Entity
@Table(name = "reports")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ReportEntity {
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "report_id")
	private Long id;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "reporter_id", nullable = false)
	private UserEntity reporter;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "target_post_id", nullable = false)
	private JobPostingEntity targetPost;
	
	@Column(name = "reason_category", length = 50)
	private String reasonCategory;
	
	@Column(columnDefinition = "TEXT")
	private String description;
	
	@Enumerated(EnumType.STRING)
	@Column(length = 20)
	private ReportStatus status;
	
	@CreationTimestamp
	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;
}
