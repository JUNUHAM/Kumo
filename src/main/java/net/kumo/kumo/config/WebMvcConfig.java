package net.kumo.kumo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    // application.properties에서 정한 상대 경로 'chat_uploads/'를 가져옵니다.
    @Value("${file.upload.chat}")
    private String chatUploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // [핵심] 프로젝트가 실행되는 현재 루트 경로를 자동으로 가져옵니다.
        String rootPath = System.getProperty("user.dir");

        // 1. 브라우저 주소창에 들어갈 URL 패턴: /chat_images/파일명.jpg
        // 2. 실제 파일이 저장된 로컬 위치: 프로젝트루트/chat_uploads/
        registry.addResourceHandler("/chat_images/**")
                .addResourceLocations("file:" + rootPath + "/" + chatUploadDir);
    }

}