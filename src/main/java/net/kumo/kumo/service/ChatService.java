package net.kumo.kumo.service;

import lombok.RequiredArgsConstructor;
import net.kumo.kumo.domain.dto.ChatMessageDTO;
import net.kumo.kumo.domain.dto.ChatRoomListDTO;
import net.kumo.kumo.domain.entity.ChatMessageEntity;
import net.kumo.kumo.domain.entity.ChatRoomEntity;
import net.kumo.kumo.domain.entity.Enum.MessageType;
import net.kumo.kumo.domain.entity.UserEntity;
import net.kumo.kumo.repository.ChatMessageRepository;
import net.kumo.kumo.repository.ChatRoomRepository;
import net.kumo.kumo.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 채팅과 관련된 핵심 비즈니스 로직을 처리하는 서비스 클래스입니다.
 * 채팅방 생성/조회, 메시지 저장/조회, 읽음 처리 등의 기능을 제공합니다.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;

    /**
     * 구직자와 구인자 간의 채팅방을 생성하거나 이미 존재하는 방을 반환합니다.
     * 동일한 공고(출처 포함)에 대해 이미 채팅방이 있다면 해당 방을 반환합니다.
     *
     * @param seekerId 구직자(Seeker)의 사용자 ID
     * @param recruiterId 구인자(Recruiter)의 사용자 ID
     * @param targetPostId 연관된 공고 ID
     * @param targetSource 공고의 출처 (예: OSAKA, TOKYO 등)
     * @return 생성되거나 조회된 ChatRoomEntity 객체
     * @throws IllegalArgumentException 사용자를 찾을 수 없을 때 발생
     */
    public ChatRoomEntity createOrGetChatRoom(Long seekerId, Long recruiterId, Long targetPostId, String targetSource) {

        Optional<ChatRoomEntity> existingRoom = chatRoomRepository
                .findBySeeker_UserIdAndRecruiter_UserIdAndTargetPostIdAndTargetSource(
                        seekerId, recruiterId, targetPostId, targetSource);

        if (existingRoom.isPresent()) {
            return existingRoom.get();
        }

        UserEntity seeker = userRepository.findById(seekerId)
                .orElseThrow(() -> new IllegalArgumentException("구직자를 찾을 수 없습니다."));

        UserEntity recruiter = userRepository.findById(recruiterId)
                .orElseThrow(() -> new IllegalArgumentException("구인자를 찾을 수 없습니다."));

        ChatRoomEntity newRoom = ChatRoomEntity.builder()
                .seeker(seeker)
                .recruiter(recruiter)
                .targetPostId(targetPostId)
                .targetSource(targetSource)
                .build();

        return chatRoomRepository.save(newRoom);
    }

    /**
     * 특정 채팅방의 정보를 조회합니다.
     *
     * @param roomId 조회할 채팅방 ID
     * @return 조회된 ChatRoomEntity 객체
     * @throws IllegalArgumentException 채팅방이 존재하지 않을 때 발생
     */
    @Transactional(readOnly = true)
    public ChatRoomEntity getChatRoom(Long roomId) {
        return chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("방이 없습니다."));
    }

    /**
     * 사용자가 전송한 채팅 메시지를 DB에 저장합니다.
     * 실시간으로 전송되는 메시지이므로 날짜 포맷은 기본값("kr")으로 처리합니다.
     *
     * @param dto 저장할 메시지 정보가 담긴 DTO
     * @return 저장 후 변환된 ChatMessageDTO 객체
     */
    public ChatMessageDTO saveMessage(ChatMessageDTO dto) {
        ChatRoomEntity room = getChatRoom(dto.getRoomId());

        UserEntity sender = userRepository.findById(dto.getSenderId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        ChatMessageEntity entity = ChatMessageEntity.builder()
                .room(room)
                .sender(sender)
                .content(dto.getContent())
                .messageType(MessageType.valueOf(dto.getMessageType()))
                .isRead(false)
                .build();

        ChatMessageEntity saved = chatMessageRepository.save(entity);
        return convertToDTO(saved, "kr");
    }

    /**
     * 특정 채팅방의 과거 메시지 기록을 조회하고, 읽음 처리를 수행합니다.
     * 사용자의 언어 설정에 따라 날짜 포맷이 다르게 적용됩니다.
     *
     * @param roomId 채팅방 ID
     * @param userId 메시지를 읽는 사용자의 ID
     * @param lang 사용자의 현재 언어 설정 (예: "ko", "ja")
     * @return 언어 포맷이 적용된 과거 메시지 DTO 리스트
     */
    public List<ChatMessageDTO> getMessageHistory(Long roomId, Long userId, String lang) {
        chatMessageRepository.markMessagesAsRead(roomId, userId);

        return chatMessageRepository.findByRoom_IdOrderByCreatedAtAsc(roomId)
                .stream()
                .map(entity -> convertToDTO(entity, lang))
                .collect(Collectors.toList());
    }

    /**
     * 특정 사용자가 참여 중인 모든 채팅방 목록을 조회합니다.
     * 상대방의 닉네임, 프로필 이미지, 마지막 메시지, 안 읽은 메시지 여부 등을 포함합니다.
     *
     * @param userId 조회할 사용자의 ID
     * @return 채팅방 목록 정보가 담긴 DTO 리스트
     */
    @Transactional(readOnly = true)
    public List<ChatRoomListDTO> getChatRoomsForUser(Long userId) {

        List<ChatRoomEntity> rooms = chatRoomRepository.findChatRoomsByUserId(userId);

        return rooms.stream().map(room -> {
            UserEntity opponent = room.getSeeker().getUserId().equals(userId) ? room.getRecruiter() : room.getSeeker();

            String profileImg = "/images/common/default_profile.png";
            if (opponent.getProfileImage() != null && opponent.getProfileImage().getFileUrl() != null) {
                profileImg = opponent.getProfileImage().getFileUrl();
            }

            ChatMessageEntity lastMsg = chatMessageRepository.findFirstByRoomOrderByCreatedAtDesc(room);
            boolean hasUnreadFlag = chatMessageRepository.existsByRoomAndSender_UserIdNotAndIsReadFalse(room, userId);

            return ChatRoomListDTO.builder()
                    .roomId(room.getId())
                    .opponentNickname(opponent.getNickname())
                    .opponentProfileImg(profileImg)
                    .lastMessage(lastMsg != null ? lastMsg.getContent() : "대화 내용이 없습니다.")
                    .lastTime(lastMsg != null ? lastMsg.getCreatedAt().format(DateTimeFormatter.ofPattern("HH:mm")) : "")
                    .hasUnread(hasUnreadFlag)
                    .build();
        }).collect(Collectors.toList());
    }

    /**
     * 웹소켓을 통한 실시간 메시지 읽음 신호를 처리합니다.
     *
     * @param roomId 채팅방 ID
     * @param readerId 메시지를 읽은 사용자의 ID
     */
    public void processLiveReadSignal(Long roomId, Long readerId) {
        chatMessageRepository.markMessagesAsRead(roomId, readerId);
    }

    /**
     * 특정 사용자가 아직 읽지 않은 총 메시지 개수를 반환합니다.
     *
     * @param userId 사용자 ID
     * @return 안 읽은 메시지 총 개수
     */
    @Transactional(readOnly = true)
    public int getUnreadMessageCount(Long userId) {
        return chatMessageRepository.countUnreadMessagesForUser(userId);
    }

    /**
     * ChatMessageEntity를 ChatMessageDTO로 변환하는 헬퍼 메서드입니다.
     * 전달받은 언어 파라미터에 따라 채팅창 구분선에 쓰일 날짜 포맷을 다국어로 적용합니다.
     *
     * @param entity 변환할 메시지 엔티티
     * @param lang 적용할 언어 코드 (예: "ja", "kr")
     * @return 변환된 DTO 객체
     */
    private ChatMessageDTO convertToDTO(ChatMessageEntity entity, String lang) {
        java.util.Locale locale = "ja".equals(lang) ? java.util.Locale.JAPANESE : java.util.Locale.KOREAN;
        String datePattern = "ja".equals(lang) ? "yyyy年 M月 d日 EEEE" : "yyyy년 M월 d일 EEEE";

        return ChatMessageDTO.builder()
                .roomId(entity.getRoom().getId())
                .senderId(entity.getSender() != null ? entity.getSender().getUserId() : null)
                .senderNickname(entity.getSender() != null ? entity.getSender().getNickname() : "알 수 없음")
                .content(entity.getContent())
                .messageType(entity.getMessageType().name())
                .createdAt(entity.getCreatedAt().format(DateTimeFormatter.ofPattern("HH:mm")))
                .createdDate(entity.getCreatedAt().format(java.time.format.DateTimeFormatter.ofPattern(datePattern, locale)))
                .isRead(entity.getIsRead())
                .build();
    }
}