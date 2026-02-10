package net.kumo.kumo.service.chat;

import lombok.RequiredArgsConstructor;
import net.kumo.kumo.domain.chat.ChatMessage;
import net.kumo.kumo.domain.chat.ChatRoom;
import net.kumo.kumo.repository.chat.ChatMessageRepository;
import net.kumo.kumo.repository.chat.ChatRoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;

    /**
     * 1. 방 만들기 (또는 찾기)
     * - 구직자가 "문의하기" 버튼을 눌렀을 때 실행됩니다.
     * - DB에 이미 둘의 방이 있으면 그걸 주고, 없으면 새로 만듭니다.
     */
    public ChatRoom createOrGetChatRoom(String seekerId, String recruiterId) {
        // 1. 먼저 DB에서 둘의 방이 있는지 찾아봅니다.
        Optional<ChatRoom> existingRoom = chatRoomRepository.findBySeekerIdAndRecruiterId(seekerId, recruiterId);

        // 2. 방이 있으면? -> 그 방을 바로 리턴!
        if (existingRoom.isPresent()) {
            return existingRoom.get();
        }

        // 3. 방이 없으면? -> 새로 만들어서 저장하고 리턴!
        ChatRoom newRoom = ChatRoom.builder()
                .seekerId(seekerId)
                .recruiterId(recruiterId)
                .build();

        return chatRoomRepository.save(newRoom);
    }

    /**
     * 2. 메시지 저장하기
     * - 채팅을 칠 때마다 이 메서드가 호출되어 DB에 저장됩니다.
     */
    public ChatMessage saveMessage(ChatMessage message) {
        return chatMessageRepository.save(message);
    }

    /**
     * 3. 대화 기록 가져오기
     * - 채팅방에 처음 입장했을 때, 이전 대화를 불러옵니다.
     */
    @Transactional(readOnly = true) // 읽기 전용 (성능 최적화)
    public List<ChatMessage> getMessageHistory(Long roomId) {
        return chatMessageRepository.findByRoomIdOrderBySentAtAsc(roomId);
    }

    /**
     * 4. 내 채팅방 목록 가져오기 (구직자용)
     */
    @Transactional(readOnly = true)
    public List<ChatRoom> getSeekerChatRooms(String seekerId) {
        return chatRoomRepository.findBySeekerId(seekerId);
    }

    /**
     * 5. 내 채팅방 목록 가져오기 (구인자용)
     */
    @Transactional(readOnly = true)
    public List<ChatRoom> getRecruiterChatRooms(String recruiterId) {
        return chatRoomRepository.findByRecruiterId(recruiterId);
    }
}