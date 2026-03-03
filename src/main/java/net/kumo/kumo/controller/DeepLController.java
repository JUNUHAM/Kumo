package net.kumo.kumo.controller;

import net.kumo.kumo.domain.dto.TranslationRequestDTO;
import net.kumo.kumo.domain.dto.TranslationResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/translate")
public class DeepLController {

    @Value("${deepl.api.key}")
    private String apiKey;

    @Value("${deepl.api.url}")
    private String apiUrl;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TranslationResponseDTO> translate(@RequestBody TranslationRequestDTO request) {

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "DeepL-Auth-Key " + apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        Map<String, Object> body = new HashMap<>();
        body.put("text", request.getText());
        body.put("target_lang", request.getTarget_lang());

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            TranslationResponseDTO response = restTemplate.postForObject(apiUrl, entity, TranslationResponseDTO.class);
            return ResponseEntity.ok(response);
            
        // =================================================================
        // ★★★ 여기가 핵심입니다! 가짜 로그들 사이에서 진짜 범인을 잡아내는 덫! ★★★
        // =================================================================
        } catch (org.springframework.web.client.HttpStatusCodeException e) {
            System.out.println("\n==================================================");
            System.out.println("🚨 [범인 검거] DeepL API가 번역을 거절했습니다!");
            System.out.println("거절 사유: " + e.getResponseBodyAsString());
            System.out.println("상태 코드: " + e.getStatusCode());
            System.out.println("==================================================\n");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            
        } catch (Exception e) {
            System.out.println("\n==================================================");
            System.out.println("🚨 [범인 검거] 스프링 내부에서 오류가 발생했습니다!");
            System.out.println("에러 메시지: " + e.getMessage());
            System.out.println("==================================================\n");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}