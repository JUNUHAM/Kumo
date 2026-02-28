package net.kumo.kumo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    // application.properties에 설정한 경로를 가져옵니다.
    // (예: /Users/hyunwookim/Desktop/kumo_uploads/)
    @Value("${file.upload.dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 1. 브라우저에서 접근하는 URL 패턴: /images/uploadFile/**
        // 2. 실제 파일이 있는 로컬 경로: file:///Users/hyunwookim/Desktop/kumo_uploads/

        registry.addResourceHandler("/images/uploadFile/**")
                .addResourceLocations("file:" + uploadDir); // 'file:' 접두어가 중요합니다!
    }
}