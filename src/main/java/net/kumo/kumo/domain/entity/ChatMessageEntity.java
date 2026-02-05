package net.kumo.kumo.domain.entity;


import jakarta.persistence.*;
import lombok.*;
import net.kumo.kumo.domain.entity.Enum.MessageType;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ChatMessageEntity {
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "msg_id")
	private Long id;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "room_id", nullable = false)
	private ChatRoomEntity room;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "sender_id", nullable = false)
	private UserEntity sender;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "message_type", length = 20)
	private MessageType messageType;
	
	@Column(columnDefinition = "TEXT")
	private String content;
	
	@Column(name = "is_read")
	private Boolean isRead;
	
	@CreationTimestamp
	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;
}
