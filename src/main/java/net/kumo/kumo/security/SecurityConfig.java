package net.kumo.kumo.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http
                                .authorizeHttpRequests((auth) -> auth
                                                // ★ 여기가 핵심! ★
                                                // 기존 허용 목록에 채팅 관련 주소를 추가합니다.
                                                .requestMatchers(
                                                                "/",
                                                                "/login",
                                                                "/signup/**",
                                                                "/company_info",
                                                                "/css/**",
                                                                "/images/**",
                                                                "/js/**",
                                                                "/seekerView/**",
                                                                "/error",
                                                                "/seeker/**",

                                                                // 👇 [채팅 기능 추가] 👇
                                                                "/chat/**", // 채팅 목록 및 채팅방 화면
                                                                "/ws-stomp/**" // 웹소켓 통신 연결 주소
                                                )
                                                .permitAll()
                                                .anyRequest().authenticated())
                                .formLogin((form) -> form
                                                .loginPage("/login") // 로그인 필요할 땐 여기로 보내라
                                                .permitAll());

                return http.build();
        }
}