package net.kumo.kumo.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "reports")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ReportEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long reportId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private UserEntity reporter;

    @Column(name = "target_post_id", nullable = false)
    private Long targetPostId;

    @Column(name = "target_source", nullable = false, length = 50)
    private String targetSource;

    @Column(name = "reason_category", nullable = false, length = 50)
    private String reasonCategory;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // ★ [수정] Enum 대신 String 사용
    // DB의 ENUM 값('PENDING', 'CLOSED', 'CHECKED')이 문자열로 들어옵니다.
    @Column(name = "status", nullable = false)
    @Builder.Default
    private String status = "PENDING"; // 기본값 설정

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // 상태 변경 메서드 (문자열로 받음)
    public void updateStatus(String newStatus) {
        this.status = newStatus;
    }
}