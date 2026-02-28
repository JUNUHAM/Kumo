package net.kumo.kumo.config; // 패키지명 수정됨

import java.time.Duration;
import java.util.Locale;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

/**
 * 
 */
@Configuration
public class LocaleConfig implements WebMvcConfigurer {

	@Bean
	public LocaleResolver localeResolver() {
		CookieLocaleResolver resolver = new CookieLocaleResolver();
		resolver.setDefaultLocale(Locale.KOREAN); // 기본 한국어
		resolver.setCookieName("lang"); // 쿠키 이름
		resolver.setCookieMaxAge(Duration.ofDays(1)); // 하루 정도
		return resolver;
	}

	@Bean
	public LocaleChangeInterceptor localeChangeInterceptor() {
		LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
		interceptor.setParamName("lang"); // URL 파라미터 감지 (?lang=ja)
		return interceptor;
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(localeChangeInterceptor());
	}

	// 원우 전용 사진 업로드 오류수정
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		// 1. 사장님 맥북의 실제 저장 위치 (반드시 끝에 /를 붙이세요!)
		String actualPath = "file:" + System.getProperty("user.home") + "/kumo_uploads/profiles/";

		// 2. 브라우저가 사용하는 가상 주소 (/upload/profiles/...)
		// 이 주소로 요청이 오면 actualPath 폴더에서 파일을 찾으라고 명령합니다.
		registry.addResourceHandler("/upload/profiles/**")
				.addResourceLocations(actualPath);

		// 캐시 설정 (이미지가 즉시 안 바뀔 때를 대비)
		registry.addResourceHandler("/upload/profiles/**")
				.addResourceLocations(actualPath)
				.setCachePeriod(0); // 0으로 설정하면 즉시 반영됩니다.
	}
}