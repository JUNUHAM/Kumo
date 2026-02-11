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
    private Long reportId;      // 어드민 조회용 추가
    private Long reporterId;
    private Long targetPostId;
    private String targetSource;

    private String reasonCategory;
    private String description;

    private String status;      // 어드민 조회용 추가
    private LocalDateTime createdAt; // 어드민 조회용 추가

    // [어드민용] Entity -> DTO 변환 (파싱 로직 포함)
    public static ReportDTO fromEntity(ReportEntity entity) {
        String fullDesc = entity.getDescription();
        String source = "UNKNOWN";
        String realDesc = fullDesc;

        // " [OSAKA] 내용 " 형태에서 출처 추출
        if (fullDesc != null && fullDesc.startsWith("[")) {
            int endIdx = fullDesc.indexOf("]");
            if (endIdx > 0) {
                source = fullDesc.substring(1, endIdx);
                realDesc = fullDesc.substring(endIdx + 1).trim();
            }
        }

        return ReportDTO.builder()
                .reportId(entity.getReportId())
                .reporterId(entity.getReporterId())
                .targetPostId(entity.getTargetPostId())
                .targetSource(source)
                .description(realDesc)
                .reasonCategory(entity.getReasonCategory())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}