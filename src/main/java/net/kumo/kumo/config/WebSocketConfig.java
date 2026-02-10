package net.kumo.kumo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker // "웹소켓 기능을 켜라!" 라는 뜻
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 1. 연결(Handshake) 엔드포인트 설정
        // 클라이언트(JS)가 서버에 접속할 때 "ws://localhost:8080/ws-stomp" 로 접속하게 함
        registry.addEndpoint("/ws-stomp")
                .setAllowedOriginPatterns("*") // 모든 도메인에서 접속 허용 (CORS 방지)
                .withSockJS(); // 웹소켓을 지원하지 않는 구형 브라우저도 지원하게 함
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 2. 메시지 구독(Subscribe) 요청 prefix
        // 클라이언트가 "메시지 주세요~" 할 때 "/sub"로 시작하는 주소를 씀
        // 예: /sub/chat/room/1 (1번 방 소식 듣기)
        registry.enableSimpleBroker("/sub");

        // 3. 메시지 발행(Publish) 요청 prefix
        // 클라이언트가 "메시지 보냅니다!" 할 때 "/pub"로 시작하는 주소를 씀
        // 예: /pub/chat/message (메시지 보내기)
        registry.setApplicationDestinationPrefixes("/pub");
    }
}