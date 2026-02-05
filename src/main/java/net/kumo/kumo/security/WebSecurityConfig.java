package net.kumo.kumo.security;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				// 1. 페이지 접근 권한 설정
				.authorizeHttpRequests((authorize) -> authorize
						// 정적 리소스(css, js, images)와 메인, 로그인, 회원가입 페이지는 누구나 접근 가능
//						.requestMatchers("/css/**", "/js/**", "/images/**", "/error").permitAll()
//						.requestMatchers("/", "/login", "/signup", "/join").permitAll()

						// 관리자 페이지는 'ADMIN' 권한이 있는 사람만 접근 가능
//						.requestMatchers("/admin/**").hasRole("ADMIN")

						// 그 외 모든 요청은 인증(로그인)된 사용자만 접근 가능
//						.anyRequest().authenticated()

						// 작업용으로 일단 만들어둠
								.anyRequest().permitAll()
				)

				// 2. 로그인 설정 (Form Login)
				.formLogin((form) -> form
						.loginPage("/login")             // 사용자가 만든 커스텀 로그인 페이지 URL
						.loginProcessingUrl("/loginProc")// HTML Form의 action과 일치해야 함
						.usernameParameter("email")      // 로그인 폼의 아이디 input name (기본값: username)
						.passwordParameter("password")   // 로그인 폼의 비번 input name (기본값: password)
						.defaultSuccessUrl("/", true)    // 로그인 성공 시 이동할 페이지
						.permitAll()
				)

				// 3. 로그아웃 설정
				.logout((logout) -> logout
						.logoutUrl("/logout")            // 로그아웃 URL 지정 (기본값 POST)
						.logoutSuccessUrl("/")           // 성공 시 이동할 주소
						.invalidateHttpSession(true)     // 세션 삭제
						.deleteCookies("JSESSIONID")     // 쿠키 삭제 (선택 사항)
				);

		// 4. CSRF 설정 (필요 시)
		// .csrf((csrf) -> csrf.ignoringRequestMatchers("/api/**")) // API 통신 시 CSRF 끌 때 사용

		return http.build();
	}

	// 비밀번호 암호화 빈 등록 (필수!)
	// DB에 비밀번호를 그대로 저장하지 않고 암호화해서 저장/비교하기 위해 필요합니다.
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}