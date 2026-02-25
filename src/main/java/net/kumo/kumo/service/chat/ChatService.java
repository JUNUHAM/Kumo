package net.kumo.kumo.service.chat;

import lombok.RequiredArgsConstructor;
import net.kumo.kumo.domain.dto.ChatMessageDTO; // ★ Step 1에서 만든 DTO 추가
import net.kumo.kumo.domain.entity.ChatMessageEntity;
import net.kumo.kumo.domain.entity.ChatRoomEntity;
import net.kumo.kumo.domain.entity.Enum.MessageType;
import net.kumo.kumo.domain.entity.UserEntity;
import net.kumo.kumo.repository.UserRepository;
import net.kumo.kumo.repository.chat.ChatMessageRepository;
import net.kumo.kumo.repository.chat.ChatRoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    // (필요 시 주석 해제하여 사용)
    // private final UserRepository userRepository;
    // private final JobPostingRepository jobPostingRepository;
    private final UserRepository userRepository;

    /**
     * 1. 방 만들기 (또는 찾기)
     * - 내용 변함 없음: 현우님의 기존 로직 유지
     */
    public ChatRoomEntity createOrGetChatRoom(Long seekerId, Long recruiterId, Long jobPostId) {
        Optional<ChatRoomEntity> existingRoom = chatRoomRepository.findBySeeker_UserIdAndRecruiter_UserId(seekerId,
                recruiterId);

        if (existingRoom.isPresent()) {
            return existingRoom.get();
        }

        /*
         * [구현 가이드 - 기존 내용 유지]
         * UserEntity seeker = userRepository.findById(seekerId).orElseThrow();
         * ... (중략) ...
         * return chatRoomRepository.save(newRoom);
         */

        return null;
    }

    /**
     * 2. 메시지 저장하기 (정석 개편: DTO 기반)
     * - 엔티티를 직접 받지 않고 DTO를 받아 변환 후 저장합니다.
     */
    // [ChatService.java]
    public ChatMessageDTO saveMessage(ChatMessageDTO dto) {
        ChatRoomEntity room = getChatRoom(dto.getRoomId());

        // ★ 1. 발신자(UserEntity)를 찾는 로직이 반드시 필요합니다.
        // (userRepository 주석을 풀고 아래처럼 연결하세요)
        UserEntity sender = userRepository.findById(dto.getSenderId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        ChatMessageEntity entity = ChatMessageEntity.builder()
                .room(room)
                .sender(sender) // ★ 이 'sender'가 빠지면 DB 저장 시 에러(500)가 발생합니다.
                .content(dto.getContent())
                .messageType(MessageType.valueOf(dto.getMessageType()))
                .isRead(false)
                .build();

        ChatMessageEntity saved = chatMessageRepository.save(entity);
        return convertToDTO(saved);
    }

    /**
     * 3. 대화 기록 가져오기 (정석 개편: List<DTO> 반환)
     */
    @Transactional(readOnly = true)
    public List<ChatMessageDTO> getMessageHistory(Long roomId) {
        return chatMessageRepository.findByRoom_IdOrderByCreatedAtAsc(roomId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * [정석 포인트] Entity를 DTO로 변환하는 내부 로직 (재사용)
     */
    private ChatMessageDTO convertToDTO(ChatMessageEntity entity) {
        return ChatMessageDTO.builder()
                .roomId(entity.getRoom().getId())
                .senderId(entity.getSender() != null ? entity.getSender().getUserId() : null)
                .senderNickname(entity.getSender() != null ? entity.getSender().getNickname() : "알 수 없음")
                .content(entity.getContent())
                .messageType(entity.getMessageType().name())
                .createdAt(entity.getCreatedAt().format(DateTimeFormatter.ofPattern("HH:mm")))
                .build();
    }

    // --- 아래 현우님의 기존 메서드들 (목록 조회 등) 내용 유지 ---

    @Transactional(readOnly = true)
    public List<ChatRoomEntity> getSeekerChatRooms(Long seekerId) {
        return chatRoomRepository.findBySeeker_UserId(seekerId);
    }

    @Transactional(readOnly = true)
    public List<ChatRoomEntity> getRecruiterChatRooms(Long recruiterId) {
        return chatRoomRepository.findByRecruiter_UserId(recruiterId);
    }

    public ChatRoomEntity getChatRoom(Long roomId) {
        return chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("방이 없습니다."));
    }

    // ChatService 클래스 내부 어디든 상관없지만, 보통 맨 아래에 넣습니다.
    public List<ChatRoomEntity> getChatRoomsForUser(Long userId) {
        // Repository에게 "내가 구직자거나 구인자인 방을 싹 다 찾아와!"라고 시킵니다.
        return chatRoomRepository.findBySeekerUserIdOrRecruiterUserId(userId, userId);
    }
}