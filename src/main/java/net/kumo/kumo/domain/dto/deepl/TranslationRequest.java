package net.kumo.kumo.domain.dto.deepl; // domain 추가

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TranslationRequest {
    private List<String> text;
    private String target_lang;
}