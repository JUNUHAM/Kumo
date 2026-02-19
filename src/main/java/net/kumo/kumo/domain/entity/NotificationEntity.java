package net.kumo.kumo.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class NotificationEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "notification_id")
	private Long id;
	
	// 수신 사용자 (FK 연결)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private UserEntity user;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "notify_type", nullable = false)
	private Enum.NotificationType notifyType;
	
	// 메시지 키를 저장 (예: "notification.chat.new")
	@Column(nullable = false)
	private String title;
	
	@Column(columnDefinition = "TEXT", nullable = false)
	private String content;
	
	private String targetUrl; // 클릭 시 이동 경로
	
	@Builder.Default
	@Column(name = "is_read")
	private boolean isRead = false;
	
	@CreationTimestamp
	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;
}