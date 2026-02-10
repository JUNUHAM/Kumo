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
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long roomId; // 채팅방 고유 번호 (1, 2, 3...)

    // 구직자 ID (예: seeker123)
    // 조원들이 만든 User 테이블과 연결해도 되지만, 일단 ID만 저장하면 에러가 안 납니다.
    private String seekerId;

    // 구인자(채용담당자) ID (예: recruiter_samsung)
    private String recruiterId;

    // 방 생성일 (언제 처음 대화를 텄는지)
    private LocalDateTime createdAt;

    // (선택) 어떤 공고 보고 연락했는지? (공고 ID) - 나중에 필요하면 추가하세요!
    // private Long jobId;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now(); // 저장되기 직전에 현재 시간 자동 입력
    }
}