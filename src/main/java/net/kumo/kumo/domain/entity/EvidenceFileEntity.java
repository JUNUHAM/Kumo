package net.kumo.kumo.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "evidence_file")
public class EvidenceFileEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(nullable = false)
	private String fileName; // 저장된 파일명 (UUID_원본파일명.jpg)
	
	private String fileType; // 파일 종류 (EVIDENCE, PROFILE 등)
	
	// ★ UserEntity와 다대일(N:1) 관계 설정
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id") // DB 컬럼명
	private UserEntity user;
}