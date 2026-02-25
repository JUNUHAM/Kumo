package net.kumo.kumo.repository.chat;

import net.kumo.kumo.domain.entity.ChatMessageEntity;
import net.kumo.kumo.domain.entity.ChatRoomEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessageEntity, Long> {

    // 1. 전체 대화 기록 조회 (채팅방 진입 시)
    List<ChatMessageEntity> findByRoom_IdOrderByCreatedAtAsc(Long roomId);

    // 2. 최신 메시지 1건 조회 (채팅 목록 출력 시)
    ChatMessageEntity findFirstByRoomOrderByCreatedAtDesc(ChatRoomEntity room);
}