package net.kumo.kumo.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportDTO {
    private Long reporterId;    // 신고자 ID (세션에서 추출 예정)
    private Long targetPostId;  // 신고 대상 공고 ID
    private String targetSource; // 오사카/도쿄 등 (DB에는 없지만 로직 처리를 위해 받음)

    private String reasonCategory; // spam, false_info, abuse, other
    private String description;    // 상세 내용
}