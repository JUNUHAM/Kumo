package net.kumo.kumo.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "scout_offers")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ScoutOfferEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long scoutId;

    // 제안을 보낸 구인자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recruiter_id", nullable = false)
    private UserEntity recruiter;

    // 제안을 받은 구직자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seeker_id", nullable = false)
    private UserEntity seeker;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private ScoutStatus status = ScoutStatus.PENDING;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    public enum ScoutStatus {
        PENDING, ACCEPTED, REJECTED
    }
}
