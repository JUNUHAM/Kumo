package net.kumo.kumo.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "login_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class LoginHistoryEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long historyId;
	
	@Column(nullable = false)
	private String email;
	
	@Column(nullable = false, length = 50)
	private String clientIp;
	
	private String userAgent; // 브라우저 정보 (Chrome, Safari 등)
	
	@Column(nullable = false)
	private boolean isSuccess; // 성공: true, 실패: false
	
	private String failReason; // 실패 이유 기록
	
	@CreationTimestamp // INSERT 시 시간 자동 저장
	private LocalDateTime attemptTime;
}