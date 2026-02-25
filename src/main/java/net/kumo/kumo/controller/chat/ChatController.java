package net.kumo.kumo.controller.chat;

import lombok.RequiredArgsConstructor;
import net.kumo.kumo.domain.dto.ChatMessageDTO;
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

    // [이식 포인트] application.properties의 file.upload.chat 값을 가져옵니다.
    @Value("${file.upload.chat}")
    private String chatUploadDir;

    // ======================================================================
    // 1. [화면 연결] 웹 페이지 이동 관련 (HTTP)
    // ======================================================================

    @GetMapping("/chat/room/{roomId}")
    public String enterRoom(@PathVariable("roomId") Long roomId,
            @RequestParam(value = "userId", required = false) Long userId,
            Model model) {

        if (userId == null) {
            return "redirect:/chat/list";
        }

        ChatRoomEntity room = chatService.getChatRoom(roomId);
        List<ChatMessageDTO> history = chatService.getMessageHistory(roomId);

        UserEntity opponent;
        if (room.getSeeker().getUserId().equals(userId)) {
            opponent = room.getRecruiter();
        } else {
            opponent = room.getSeeker();
        }

        model.addAttribute("roomId", roomId);
        model.addAttribute("userId", userId);
        model.addAttribute("history", history);

        model.addAttribute("roomName", opponent.getNickname());
        model.addAttribute("jobTitle", room.getJobPosting().getTitle());
        model.addAttribute("salary", room.getJobPosting().getSalaryAmount());
        model.addAttribute("address", room.getJobPosting().getWorkAddress());

        return "chat/chat_room";
    }

    @GetMapping("/chat/list")
    public String chatList(
            @RequestParam(value = "userId", required = false) Long userId,
            Model model) {

        if (userId == null) {
            return "redirect:/login";
        }

        List<ChatRoomEntity> chatRooms = chatService.getChatRoomsForUser(userId);

        model.addAttribute("chatRooms", chatRooms);
        model.addAttribute("userId", userId);

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

            // [이식 포인트] 프로젝트 루트 경로를 기준으로 절대 경로 계산 (조원 공용)
            String rootPath = System.getProperty("user.dir");
            String fullPath = rootPath + "/" + chatUploadDir;

            String originalFilename = file.getOriginalFilename();
            String savedFilename = UUID.randomUUID().toString() + "_" + originalFilename;

            File folder = new File(fullPath);
            if (!folder.exists())
                folder.mkdirs();

            File dest = new File(fullPath + savedFilename);
            file.transferTo(dest);

            // [이식 포인트] WebMvcConfig에서 정한 /chat_images/ 주소로 리턴
            return ResponseEntity.ok("/chat_images/" + savedFilename);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("업로드 실패");
        }
    }

    // ======================================================================
    // 3. [실시간 통신] 메시지 주고 받기 (WebSocket)
    // ======================================================================

    @MessageMapping("/chat/message")
    public void sendMessage(ChatMessageDTO messageDTO) {
        ChatMessageDTO savedMessage = chatService.saveMessage(messageDTO);
        messagingTemplate.convertAndSend("/sub/chat/room/" + savedMessage.getRoomId(), savedMessage);
    }
}