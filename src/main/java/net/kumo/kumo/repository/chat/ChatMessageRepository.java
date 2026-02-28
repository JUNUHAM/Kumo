package net.kumo.kumo.repository.chat;

import net.kumo.kumo.domain.entity.ChatMessageEntity; // ★ 수정 1: 팀원들의 Entity import
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessageEntity, Long> { // ★ 수정 2: 클래스 이름 변경

    // 1. 대화 기록 가져오기
    // 팀원 Entity는 'room'이라는 객체로 연결되어 있고, 시간은 'createdAt'을 씁니다.
    // 따라서 "Room 객체의 Id를 기준으로 찾고, CreatedAt 순서대로 정렬해라"라고 명령해야 합니다.
    List<ChatMessageEntity> findByRoom_IdOrderByCreatedAtAsc(Long roomId);

    // (선택) 안 읽은 메시지 개수 세기 (나중에 필요하면 주석 해제)
    // int countByRoom_IdAndSender_IdNotAndIsReadFalse(Long roomId, Long myId);
}