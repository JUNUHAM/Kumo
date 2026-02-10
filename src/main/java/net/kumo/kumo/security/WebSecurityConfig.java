package net.kumo.kumo.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				// 1. CSRF 보안 설정
				// (현재는 꺼져있으므로 POST 요청 시 403 에러는 안 납니다.)
				.csrf(AbstractHttpConfigurer::disable)

				// 2. 권한 설정 (authorizeHttpRequests -> 람다식)
				.authorizeHttpRequests((auth) -> auth
						// (1) 정적 리소스: css, js, images 등
						.requestMatchers("/css/**", "/js/**", "/images/**", "/error").permitAll()

						// (2) 로그인 없이 접근 가능한 페이지
						.requestMatchers("/", "/login", "/signup", "/join", "/join/**", "/info").permitAll()
						.requestMatchers("/map_non_login_view", "/FindId", "/FindPw", "/findIdProc", "/nickname")
						.permitAll()
						.requestMatchers("/Recruiter/**").permitAll() // 테스트용

						// ★★★ [여기 추가] AJAX 중복확인 API는 로그인 없이 접근 가능해야 함 ★★★
						.requestMatchers("/api/check/**", "/api/**").permitAll()

						// 👇👇👇 [채팅 기능] 👇👇👇
						.requestMatchers("/chat/**", "/ws-stomp/**").permitAll()

						// (3) 관리자 전용
						.requestMatchers("/admin/**").hasRole("ADMIN")

						// (4) 그 외 모든 요청은 인증 필요
						.anyRequest().authenticated())

				// 3. 로그인 설정
				.formLogin((form) -> form
						.loginPage("/login")
						.loginProcessingUrl("/loginProc")
						.usernameParameter("email")
						.passwordParameter("password")
						.defaultSuccessUrl("/", true)
						.permitAll())

				// 4. 로그아웃 설정
				.logout((logout) -> logout
						.logoutUrl("/logout")
						.logoutSuccessUrl("/")
						.invalidateHttpSession(true)
						.deleteCookies("JSESSIONID"));

		return http.build();
	}

	// 비밀번호 암호화 빈
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}