package net.kumo.kumo.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.sql.Timestamp;

@Entity
@Table(name = "reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long reportId;

    @Column(name = "reporter_id", nullable = false)
    private Long reporterId;

    @Column(name = "target_post_id")
    private Long targetPostId;

    // HTML select의 value 값들이 들어감 (spam, false_info, abuse, other)
    @Column(name = "reason_category", length = 50)
    private String reasonCategory;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "ENUM('PENDING', 'CLOSED', 'BLOCKED') DEFAULT 'PENDING'")
    @Builder.Default
    private String status = "PENDING";

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Timestamp createdAt;
}