package net.kumo.kumo.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.kumo.kumo.domain.entity.ReportEntity;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportDTO {
    private Long reportId;

    // 신고자 정보
    private Long reporterId;
    private String reporterEmail; // Service에서 채워 넣음

    // 대상 공고 정보
    private Long targetPostId;
    private String targetSource;  // 이제 파싱 안 하고 DB 컬럼 값 사용!
    private String targetPostTitle; // Service에서 채워 넣음

    // 신고 내용
    private String reasonCategory;
    private String description;   // 파싱 없이 순수 본문 그대로

    private String status;
    private LocalDateTime createdAt;

    // Entity -> DTO 변환
    public static ReportDTO fromEntity(ReportEntity entity) {

        // 1. 신고자 ID 추출 (Null Safety 처리)
        Long rId = (entity.getReporter() != null) ? entity.getReporter().getUserId() : null;

        return ReportDTO.builder()
                .reportId(entity.getReportId())
                .reporterId(rId)
                .reporterEmail("-") // Service에서 채울 예정

                .targetPostId(entity.getTargetPostId())
                .targetSource(entity.getTargetSource()) // ★ 핵심: DB 컬럼 값 바로 사용
                .targetPostTitle("-") // Service에서 채울 예정

                .description(entity.getDescription()) // ★ 핵심: 파싱 로직 제거
                .reasonCategory(entity.getReasonCategory())

                .status(entity.getStatus()) // Enum이든 String이든 그대로 전달
                .createdAt(entity.getCreatedAt())
                .build();
    }
}