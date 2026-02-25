package net.kumo.kumo.domain.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class JobManageListDTO {
    private Long id; // 🌟 [추가] 진짜 테이블 PK (보기 페이지 이동용)
    private Long datanum; // 고유 식별 번호 (수정/삭제 시 필요)
    private String title; // 공고 제목
    private String regionType; // "도쿄" or "오사카" (출력용)
    private String wage; // 급여
    private LocalDateTime createdAt; // 등록일 (정렬용)
    private String status; // "RECRUITING" or "CLOSED"
}