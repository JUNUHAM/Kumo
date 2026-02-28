package net.kumo.kumo.controller.chat;

import lombok.RequiredArgsConstructor;
import net.kumo.kumo.domain.entity.ChatMessageEntity; // ★ 최신 Entity
import net.kumo.kumo.domain.entity.ChatRoomEntity; // ★ 최신 Entity
import net.kumo.kumo.service.chat.ChatService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    // application.properties에 설정한 파일 저장 경로를 가져옵니다.
    @Value("${file.upload.dir}")
    private String uploadDir;

    // ======================================================================
    // 1. [화면 연결] 웹 페이지 이동 관련 (HTTP)
    // ======================================================================

    /**
     * [채팅방 생성 또는 입장]
     * - ★ 변경: 구인공고 ID(jobPostId)가 필수로 필요합니다!
     * - (임시로 jobPostId를 1L 등으로 고정하거나, HTML에서 넘겨줘야 합니다.)
     */
    @PostMapping("/chat/room")
    public String createOrEnterRoom(@RequestParam("seekerId") Long seekerId,
            @RequestParam("recruiterId") Long recruiterId,
            @RequestParam(value = "jobPostId", defaultValue = "1") Long jobPostId) {
        // 주의: HTML에서 jobPostId를 안 넘겨주면 에러나므로 일단 기본값 1로 설정함

        ChatRoomEntity room = chatService.createOrGetChatRoom(seekerId, recruiterId, jobPostId);

        // 방이 안 만들어졌으면(null) 에러 페이지나 목록으로 (임시 방어 로직)
        if (room == null)
            return "redirect:/chat/list?userId=" + seekerId;

        // 방을 찾았으니 이동 (Entity의 ID 필드명이 'id'이므로 getId() 사용)
        return "redirect:/chat/room/" + room.getId() + "?userId=" + seekerId;
    }

    /**
     * [채팅방 입장 화면]
     * - ★ 수정됨: 헤더에 표시할 공고 정보(Job)와 상대방 정보(Opponent)를 같이 보냄
     */
    @GetMapping("/chat/room/{roomId}")
    public String enterRoom(@PathVariable("roomId") Long roomId,
            @RequestParam("userId") Long userId,
            Model model) {

        // 1. 채팅방 정보 가져오기 (이 안에 공고, 구직자, 구인자 다 들어있음)
        // (Service에 getRoom 메서드가 없다면 Repository를 바로 써도 됩니다.
        // 여기서는 chatService.getRoom(roomId)이 있다고 가정하거나,
        // chatRoomRepository.findById(roomId).get() 을 사용하세요.)
        ChatRoomEntity room = chatService.getChatRoom(roomId); // ★ Service에 이 메서드 추가 필요 (아래 참고)

        // 2. 대화 기록 가져오기
        List<ChatMessageEntity> history = chatService.getMessageHistory(roomId);

        // 3. 상대방(Opponent) 찾기
        // 내가 구직자(Seeker)면 -> 상대방은 구인자(Recruiter)
        // 내가 구인자(Recruiter)면 -> 상대방은 구직자(Seeker)
        net.kumo.kumo.domain.entity.UserEntity opponent;
        if (room.getSeeker().getUserId().equals(userId)) {
            opponent = room.getRecruiter();
        } else {
            opponent = room.getSeeker();
        }

        // 4. 모델에 데이터 담기 (HTML로 배달)
        model.addAttribute("roomId", roomId);
        model.addAttribute("userId", userId);
        model.addAttribute("history", history);

        // ★ 헤더용 데이터 추가
        model.addAttribute("roomName", opponent.getNickname()); // 상대방 이름 (또는 가게명)
        model.addAttribute("jobTitle", room.getJobPosting().getTitle()); // 공고 제목
        model.addAttribute("salary", room.getJobPosting().getSalaryAmount()); // 급여
        model.addAttribute("address", room.getJobPosting().getWorkAddress()); // 근무지 주소

        return "chat/chat_room";
    }

    /**
     * [내 채팅방 목록 보기]
     */
    @GetMapping("/chat/list")
    public String chatList(@RequestParam("userId") Long userId, Model model) {
        // (임시) 구직자라고 가정하고 목록을 가져옵니다.
        List<ChatRoomEntity> myRooms = chatService.getSeekerChatRooms(userId);

        model.addAttribute("list", myRooms);

        return "chat/chat_list";
    }

    // ======================================================================
    // 2. [기능 추가] 사진 업로드 API (Ajax 요청용)
    // ======================================================================
    @PostMapping("/chat/upload")
    @ResponseBody
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty())
                return ResponseEntity.badRequest().body("파일이 없습니다.");

            // 파일명 중복 방지 (UUID)
            String originalFilename = file.getOriginalFilename();
            String savedFilename = UUID.randomUUID().toString() + "_" + originalFilename;

            // 저장할 폴더가 없으면 생성
            File folder = new File(uploadDir);
            if (!folder.exists())
                folder.mkdirs();

            // 파일 저장
            File dest = new File(uploadDir + savedFilename);
            file.transferTo(dest);

            // 브라우저에서 접근 가능한 URL 반환 (/uploads/파일명)
            // (WebMvcConfig에서 매핑한 경로와 일치해야 함)
            return ResponseEntity.ok("/images/uploadFile/" + savedFilename);
            // 주의: 아까 properties에서 설정한 경로랑 URL 매핑이 맞아야 합니다.
            // 만약 이미지가 안 뜨면 WebMvcConfig 확인 필요!

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("업로드 실패");
        }
    }

    // ======================================================================
    // 3. [실시간 통신] 메시지 주고 받기 (WebSocket)
    // ======================================================================

    /**
     * [메시지 전송]
     * - 클라이언트에서 보낸 JSON 데이터를 ChatMessageEntity 객체로 받음
     */
    @MessageMapping("/chat/message")
    public void sendMessage(ChatMessageEntity message) {
        // 1. 메시지 저장 (DB)
        ChatMessageEntity savedMessage = chatService.saveMessage(message);

        // 2. 메시지 배달
        // Entity에서 방 번호를 꺼낼 때는 getRoom().getId()를 써야 합니다.
        // 하지만 클라이언트에서 room.id만 보냈다면 message.getRoom()이 null일 수 있습니다.
        // 일단은 안전하게 클라이언트가 보낸 방 번호를 그대로 씁니다. (DTO 사용 권장되지만, 지금은 Entity 직접 사용)

        // 주의: 클라이언트가 보낼 때 roomId를 잘 넣어서 보내야 함.
        Long currentRoomId = message.getRoom().getId();

        messagingTemplate.convertAndSend("/sub/chat/room/" + currentRoomId, savedMessage);
    }
}