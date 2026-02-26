package net.kumo.kumo.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;

@Entity
@Table(name = "scraps")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ScrapEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long scrapId;

	@Column(nullable = false)
	private Long userId;

	@Column(nullable = false)
	private Long jobPostId;

	@CreationTimestamp
	@Column(name = "created_at", updatable = false)
	private Timestamp createdAt;
	
	// 기존 컬럼들 밑에 source를 추가해 주세요.
	@Column(name = "source", length = 20)
	private String source; // TOKYO, OSAKA 등 출처 저장
}