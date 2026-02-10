package net.kumo.kumo.controller.chat;

import lombok.RequiredArgsConstructor;
import net.kumo.kumo.domain.chat.ChatMessage;
import net.kumo.kumo.domain.chat.ChatRoom;
import net.kumo.kumo.service.chat.ChatService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    // ======================================================================
    // 1. [화면 연결] 웹 페이지 이동 관련 (HTTP)
    // ======================================================================

    /**
     * [채팅방 생성 또는 입장]
     * - 구직자가 지도나 공고에서 "문의하기" 버튼을 눌렀을 때 실행됩니다.
     */
    @PostMapping("/chat/room")
    public String createOrEnterRoom(@RequestParam("seekerId") String seekerId,
            @RequestParam("recruiterId") String recruiterId) {

        ChatRoom room = chatService.createOrGetChatRoom(seekerId, recruiterId);
        // 방을 찾았으니, 그 방으로 이동합니다.
        return "redirect:/chat/room/" + room.getRoomId() + "?userId=" + seekerId;
    }

    /**
     * [채팅방 입장 화면]
     * - ★ 중요: 여기 userId 파라미터가 있는 버전 하나만 있어야 합니다!
     */
    @GetMapping("/chat/room/{roomId}")
    public String enterRoom(@PathVariable("roomId") Long roomId,
            @RequestParam("userId") String userId,
            Model model) {

        List<ChatMessage> history = chatService.getMessageHistory(roomId);

        model.addAttribute("roomId", roomId);
        model.addAttribute("userId", userId);
        model.addAttribute("history", history);

        return "chat/chat_room";
    }

    /**
     * [내 채팅방 목록 보기]
     */
    @GetMapping("/chat/list")
    public String chatList(@RequestParam("userId") String userId, Model model) {
        // (임시) 구직자라고 가정하고 목록을 가져옵니다.
        List<ChatRoom> myRooms = chatService.getSeekerChatRooms(userId);

        model.addAttribute("list", myRooms);

        return "chat/chat_list";
    }

    // ======================================================================
    // 2. [실시간 통신] 메시지 주고 받기 (WebSocket)
    // ======================================================================

    /**
     * [메시지 전송]
     */
    @MessageMapping("/chat/message")
    public void sendMessage(ChatMessage message) {
        // 1. 메시지 저장 (DB)
        ChatMessage savedMessage = chatService.saveMessage(message);

        // 2. 메시지 배달
        messagingTemplate.convertAndSend("/sub/chat/room/" + message.getRoomId(), savedMessage);
    }

    // 항가항가
}