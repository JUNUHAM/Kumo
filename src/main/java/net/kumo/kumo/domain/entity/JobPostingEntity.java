package net.kumo.kumo.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "job_postings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobPostingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "job_post_id")
    private Long jobPostId;

    // 작성자 (User 테이블과 조인)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    // 지역 (Region 테이블이 있다고 가정, 없다면 Long regionId로 대체 가능)
    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "region_id")
    // private RegionEntity region;
    @Column(name = "region_id")
    private Long regionId; // 편의상 ID로 매핑

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 100)
    private String position; // 카테고리 역할

    @Lob // LONGTEXT
    private String description;

    @Column(name = "salary_type")
    @Enumerated(EnumType.STRING)
    private SalaryType salaryType; // Enum 필요 (HOURLY, MONTHLY...)

    @Column(name = "salary_amount")
    private Integer salaryAmount;

    @Column(name = "work_address")
    private String workAddress;

    private Double latitude;
    private Double longitude;

    @Column(columnDefinition = "ENUM('RECRUITING', 'CLOSED') DEFAULT 'RECRUITING'")
    private String status;

    private LocalDate deadline;

    @Column(name = "view_count")
    private Integer viewCount;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // Enum 정의 (파일 분리해도 됨)
    public enum SalaryType {
        HOURLY, MONTHLY, DAILY, SALARY
    }
}