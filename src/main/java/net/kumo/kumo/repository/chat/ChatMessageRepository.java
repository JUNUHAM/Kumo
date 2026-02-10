package net.kumo.kumo.repository.chat;

import net.kumo.kumo.domain.chat.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // 1. 대화 기록 가져오기
    // "roomId(방번호)가 일치하는 메시지를 찾아서, sentAt(보낸시간) 순서대로(Asc) 정렬해줘"
    List<ChatMessage> findByRoomIdOrderBySentAtAsc(Long roomId);

    // (선택) 안 읽은 메시지 개수 세기 (나중에 '숫자 1' 표시할 때 사용)
    // int countByRoomIdAndSenderIdNotAndIsReadFalse(Long roomId, String myId);
}