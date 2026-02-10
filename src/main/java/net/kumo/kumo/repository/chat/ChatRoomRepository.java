package net.kumo.kumo.repository.chat;

import net.kumo.kumo.domain.chat.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    // 1. 방 찾기: 구직자와 구인자 ID로 이미 존재하는 방이 있는지 확인
    // (예: "홍길동"과 "삼성전자"가 이미 대화 중인가?)
    Optional<ChatRoom> findBySeekerIdAndRecruiterId(String seekerId, String recruiterId);

    // 2. 내 채팅방 목록 가져오기 (구직자용)
    // "내가 seekerId로 참여한 모든 방 내놔"
    List<ChatRoom> findBySeekerId(String seekerId);

    // 3. 내 채팅방 목록 가져오기 (구인자용)
    // "내가 recruiterId로 참여한 모든 방 내놔"
    List<ChatRoom> findByRecruiterId(String recruiterId);
}