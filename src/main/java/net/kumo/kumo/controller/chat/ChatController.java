package net.kumo.kumo.controller.chat;

import lombok.RequiredArgsConstructor;
import net.kumo.kumo.domain.dto.ChatMessageDTO; // ★ Step 1에서 만든 DTO 추가
import net.kumo.kumo.domain.entity.ChatRoomEntity;
import net.kumo.kumo.domain.entity.UserEntity;
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

    @Value("${file.upload.dir}")
    private String uploadDir;

    // ======================================================================
    // 1. [화면 연결] 웹 페이지 이동 관련 (HTTP)
    // ======================================================================

    @PostMapping("/chat/room")
    public String createOrEnterRoom(@RequestParam("seekerId") Long seekerId,
            @RequestParam("recruiterId") Long recruiterId,
            @RequestParam(value = "jobPostId", defaultValue = "1") Long jobPostId) {

        ChatRoomEntity room = chatService.createOrGetChatRoom(seekerId, recruiterId, jobPostId);

        if (room == null)
            return "redirect:/chat/list?userId=" + seekerId;

        return "redirect:/chat/room/" + room.getId() + "?userId=" + seekerId;
    }

    @GetMapping("/chat/room/{roomId}")
    public String enterRoom(@PathVariable("roomId") Long roomId,
            @RequestParam("userId") Long userId,
            Model model) {

        ChatRoomEntity room = chatService.getChatRoom(roomId);

        // ★ 수정 포인트: 서비스가 이제 DTO 리스트를 반환하므로 타입을 DTO로 변경
        List<ChatMessageDTO> history = chatService.getMessageHistory(roomId);

        UserEntity opponent;
        if (room.getSeeker().getUserId().equals(userId)) {
            opponent = room.getRecruiter();
        } else {
            opponent = room.getSeeker();
        }

        model.addAttribute("roomId", roomId);
        model.addAttribute("userId", userId);
        model.addAttribute("history", history); // DTO 리스트가 HTML로 전달됨

        model.addAttribute("roomName", opponent.getNickname());
        model.addAttribute("jobTitle", room.getJobPosting().getTitle());
        model.addAttribute("salary", room.getJobPosting().getSalaryAmount());
        model.addAttribute("address", room.getJobPosting().getWorkAddress());

        return "chat/chat_room";
    }

    @GetMapping("/chat/list")
    public String chatList(@RequestParam("userId") Long userId, Model model) {
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

            String originalFilename = file.getOriginalFilename();
            String savedFilename = UUID.randomUUID().toString() + "_" + originalFilename;

            File folder = new File(uploadDir);
            if (!folder.exists())
                folder.mkdirs();

            File dest = new File(uploadDir + savedFilename);
            file.transferTo(dest);

            return ResponseEntity.ok("/images/uploadFile/" + savedFilename);

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
     * - ★ 수정 포인트: 클라이언트에서 보낸 JSON 데이터를 DTO로 받음
     */
    @MessageMapping("/chat/message")
    public void sendMessage(ChatMessageDTO messageDTO) {
        // 1. 메시지 저장 및 가공 (Service에서 DTO로 처리)
        ChatMessageDTO savedMessage = chatService.saveMessage(messageDTO);

        // 2. 메시지 배달
        // DTO에 담긴 roomId를 사용하여 구독자들에게 전송
        messagingTemplate.convertAndSend("/sub/chat/room/" + savedMessage.getRoomId(), savedMessage);
    }
}