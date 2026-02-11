package net.kumo.kumo.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "reports")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ReportEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long reportId;

    @Column(name = "reporter_id", nullable = false)
    private Long reporterId;

    @Column(name = "target_post_id")
    private Long targetPostId;

    @Column(name = "reason_category", length = 50)
    private String reasonCategory;

    @Column(columnDefinition = "TEXT")
    private String description; // 여기에 "[OSAKA] 실제내용" 형태로 저장됨

    @Column(columnDefinition = "VARCHAR(20) DEFAULT 'PENDING'")
    @Builder.Default
    private String status = "PENDING";

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}