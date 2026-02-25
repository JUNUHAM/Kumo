package net.kumo.kumo.domain.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApplicationRequestDTO {
    private Long targetPostId;
    private String targetSource;
}