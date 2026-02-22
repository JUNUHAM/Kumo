package net.kumo.kumo.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "profile_images")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileImageEntity {
	
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private String originalFileName; // 사용자가 올린 원본 파일명
	private String storedFileName;   // C드라이브에 저장된 UUID 파일명
	private String fileUrl;          // 브라우저에서 접근할 URL 경로 (예: /uploads/...)
	private Long fileSize;           // 파일 크기
	
	@ToString.Exclude // 🔥 둘 중 하나, 혹은 둘 다 붙여도 좋습니다.
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private UserEntity user;
	
}
