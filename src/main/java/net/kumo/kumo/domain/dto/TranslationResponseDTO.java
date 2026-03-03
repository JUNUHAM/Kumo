package net.kumo.kumo.domain.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class TranslationResponseDTO {
    private List<TranslationData> translations;

    // ★ 핵심: Jackson(JSON 변환기)이 에러 없이 접근할 수 있도록
    // public static class로 내부에 쏙 넣어주는 것이 가장 안전한 정석입니다!
    @Getter
    @Setter
    public static class TranslationData {
        private String text;
    }
}