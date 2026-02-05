package net.kumo.kumo.domain.entity;


import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "job_images")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class JobImageEntity {
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "img_id")
	private Long id;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "job_post_id", nullable = false)
	private JobPostingEntity jobPosting;
	
	@Column(name = "image_url", nullable = false, length = 500)
	private String imageUrl;
	
}