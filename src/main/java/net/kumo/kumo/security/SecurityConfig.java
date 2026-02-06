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
                        // "/", "/css/**", "/images/**" 등은 검사 없이 통과(permitAll) 시켜라!
                        .requestMatchers("/", "/login",
                                "/signup/**", "/company_info", "/css/**", "/images/**", "/js/**", "/seekerView/**",
                                "/error")
                        .permitAll()
                        .anyRequest().authenticated())
                .formLogin((form) -> form
                        .loginPage("/login") // 로그인 필요할 땐 여기로 보내라
                        .permitAll());

        return http.build();
    }
}