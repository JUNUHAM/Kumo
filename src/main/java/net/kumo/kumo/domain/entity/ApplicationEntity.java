package net.kumo.kumo.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import net.kumo.kumo.domain.entity.Enum.ApplicationStatus;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "applications",
        // ★ DB 레벨에서도 중복 지원을 완벽 차단하는 유니크 제약조건!
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_application_source_post_seeker",
                        columnNames = {"target_source", "target_post_id", "seeker_id"}
                )
        }
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ApplicationEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "app_id")
    private Long id;

    // 1. 어느 테이블(지역/출처)의 공고인지 (예: "OSAKA", "TOKYO")
    @Column(name = "target_source", nullable = false, length = 20)
    private String targetSource;

    // 2. 해당 테이블의 몇 번 공고인지
    @Column(name = "target_post_id", nullable = false)
    private Long targetPostId;

    // 3. 지원한 구직자 (UserEntity와 다대일 관계 유지)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seeker_id", nullable = false)
    private UserEntity seeker;

    // 4. 지원 상태 (기본값 설정)
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private ApplicationStatus status = ApplicationStatus.APPLIED;

    // 5. 지원 일시
    @CreationTimestamp
    @Column(name = "applied_at", updatable = false)
    private LocalDateTime appliedAt;
}