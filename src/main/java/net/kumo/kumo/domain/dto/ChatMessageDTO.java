package net.kumo.kumo.domain.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageDTO {
    private Long roomId;
    private Long senderId;
    private String senderNickname; // 화면에 표시할 이름
    private String content;
    private String messageType; // TEXT, IMAGE
    private String createdAt; // "17:05" 처럼 포맷팅된 시간
}