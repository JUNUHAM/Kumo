package net.kumo.kumo.domain.entity;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "company_images")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyImageEntity {
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "img_id")
	private Long id;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private UserEntity user;
	
	@Column(name = "image_url", nullable = false, length = 500)
	private String imageUrl;
	
	@CreationTimestamp
	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;
}
