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
    private Long reporterId;
    private String reporterEmail;
    private Long targetPostId;
    private String targetSource;
    private String targetPostTitle;

    private String reasonCategory;
    private String description;
    private String status;
    private LocalDateTime createdAt;

    public static ReportDTO fromEntity(ReportEntity entity) {
        String fullDesc = entity.getDescription();
        String source = "INTERNAL"; // 기본값
        String realDesc = fullDesc;

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
                .targetPostTitle("-")
                .description(realDesc)
                .reasonCategory(entity.getReasonCategory())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}