package net.kumo.kumo.service.chat;

import lombok.RequiredArgsConstructor;
import net.kumo.kumo.domain.entity.ChatMessageEntity; // ★ 최신 Entity
import net.kumo.kumo.domain.entity.ChatRoomEntity; // ★ 최신 Entity
// import net.kumo.kumo.domain.entity.UserEntity;       // (필요 시 주석 해제)
// import net.kumo.kumo.domain.entity.JobPostingEntity; // (필요 시 주석 해제)
import net.kumo.kumo.repository.chat.ChatMessageRepository;
import net.kumo.kumo.repository.chat.ChatRoomRepository;
// import net.kumo.kumo.repository.UserRepository;       // (User 찾는 일꾼)
// import net.kumo.kumo.repository.JobPostingRepository; // (공고 찾는 일꾼)
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

    // ★ 주의: 방을 만들거나 메시지를 저장하려면 '진짜 유저'와 '공고' 정보가 필요합니다.
    // 만약 아래 Repository들이 없다면 잠시 주석 처리하거나, 추가로 만들어야 합니다.
    // private final UserRepository userRepository;
    // private final JobPostingRepository jobPostingRepository;

    /**
     * 1. 방 만들기 (또는 찾기)
     * - ★ 변경점: 구인공고 ID(jobPostId)가 필수로 추가되었습니다.
     */
    public ChatRoomEntity createOrGetChatRoom(Long seekerId, Long recruiterId, Long jobPostId) {
        // 1. 먼저 DB에서 둘의 방이 있는지 찾아봅니다.
        Optional<ChatRoomEntity> existingRoom = chatRoomRepository.findBySeeker_UserIdAndRecruiter_UserId(seekerId,
                recruiterId);

        // 2. 방이 있으면? -> 그 방을 바로 리턴!
        if (existingRoom.isPresent()) {
            return existingRoom.get();
        }

        // 3. 방이 없으면? -> 새로 만들어야 하는데...
        // ★ 팀원들의 Entity는 User 객체와 JobPosting 객체를 요구합니다.
        // 현재 UserRepository 코드가 없으므로, 로직만 주석으로 남겨둡니다.

        /*
         * [구현 가이드]
         * UserEntity seeker = userRepository.findById(seekerId).orElseThrow();
         * UserEntity recruiter = userRepository.findById(recruiterId).orElseThrow();
         * JobPostingEntity jobPost =
         * jobPostingRepository.findById(jobPostId).orElseThrow();
         * 
         * ChatRoomEntity newRoom = ChatRoomEntity.builder()
         * .seeker(seeker)
         * .recruiter(recruiter)
         * .jobPosting(jobPost)
         * .build();
         * 
         * return chatRoomRepository.save(newRoom);
         */

        // (임시) 일단 null 리턴 (UserRepository 생기면 위 주석 풀어서 완성하세요!)
        return null;
    }

    /**
     * 2. 메시지 저장하기
     * - 채팅을 칠 때마다 이 메서드가 호출되어 DB에 저장됩니다.
     */
    public ChatMessageEntity saveMessage(ChatMessageEntity message) {
        return chatMessageRepository.save(message);
    }

    /**
     * 3. 대화 기록 가져오기
     * - 채팅방에 처음 입장했을 때, 이전 대화를 불러옵니다.
     * - ★ 변경점: 메서드 이름이 팀원 Entity 필드명(CreatedAt)에 맞게 바뀜
     */
    @Transactional(readOnly = true)
    public List<ChatMessageEntity> getMessageHistory(Long roomId) {
        return chatMessageRepository.findByRoom_IdOrderByCreatedAtAsc(roomId);
    }

    /**
     * 4. 내 채팅방 목록 가져오기 (구직자용)
     */
    @Transactional(readOnly = true)
    public List<ChatRoomEntity> getSeekerChatRooms(Long seekerId) {
        return chatRoomRepository.findBySeeker_UserId(seekerId);
    }

    /**
     * 5. 내 채팅방 목록 가져오기 (구인자용)
     */
    @Transactional(readOnly = true)
    public List<ChatRoomEntity> getRecruiterChatRooms(Long recruiterId) {
        return chatRoomRepository.findByRecruiter_UserId(recruiterId);
    }

    public ChatRoomEntity getChatRoom(Long roomId) {
        return chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("방이 없습니다."));
    }
}