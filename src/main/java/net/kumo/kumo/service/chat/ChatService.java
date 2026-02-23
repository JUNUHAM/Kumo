package net.kumo.kumo.service.chat;

import lombok.RequiredArgsConstructor;
import net.kumo.kumo.domain.dto.ChatMessageDTO; // ★ Step 1에서 만든 DTO 추가
import net.kumo.kumo.domain.entity.ChatMessageEntity;
import net.kumo.kumo.domain.entity.ChatRoomEntity;
import net.kumo.kumo.domain.entity.Enum.MessageType;
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
    public ChatMessageDTO saveMessage(ChatMessageDTO dto) {
        // 1. DTO -> Entity 변환을 위한 정보 조회
        ChatRoomEntity room = getChatRoom(dto.getRoomId());

        // ★ 주의: 실제 구현 시에는 userRepository에서 발신자 객체를 찾아야 합니다.
        // ChatMessageEntity 생성 (기존 필드 구조 유지)
        ChatMessageEntity entity = ChatMessageEntity.builder()
                .room(room)
                .messageType(MessageType.valueOf(dto.getMessageType()))
                .content(dto.getContent())
                .isRead(false)
                .build();

        ChatMessageEntity saved = chatMessageRepository.save(entity);

        // 2. 저장된 Entity -> DTO로 다시 변환하여 반환 (포맷팅 포함)
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
}