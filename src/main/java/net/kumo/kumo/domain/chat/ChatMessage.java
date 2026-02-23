package net.kumo.kumo.domain.chat;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long messageId; // 메시지 고유 번호

    // 어느 채팅방에서 쓴 글인지? (ChatRoom의 roomId와 연결)
    private Long roomId;

    // 누가 썼는지? (보낸 사람 ID)
    private String senderId;

    // 메시지 내용 (긴 글이 될 수 있으니 Text 타입 추천)
    @Column(columnDefinition = "TEXT")
    private String message;

    // 보낸 시간
    private LocalDateTime sentAt;

    // 읽음 여부 (true: 읽음, false: 안 읽음 / 기본값은 false)
    @Builder.Default
    private boolean isRead = false;

    @PrePersist
    public void prePersist() {
        this.sentAt = LocalDateTime.now(); // 전송 시간 자동 입력
    }
}