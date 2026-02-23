package net.kumo.kumo.domain.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "job_postings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobPostingEntity {

    // 🌟 [내부 Enum 정의] 파일 따로 만들지 말고 여기에 몰아넣기
    public enum SalaryType {
        HOURLY, MONTHLY, DAILY, SALARY
    }

    public enum JobStatus {
        RECRUITING, CLOSED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "job_post_id")
    private Long jobPostId;

    // 작성자 (User)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    // 🌟 [추가됨] 회사 정보 (이게 있어야 선택한 회사가 저장됩니다!)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private CompanyEntity company;

    @Column(name = "region_id")
    private Long regionId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 100)
    private String position;

    @Lob
    private String description;

    // 급여 종류 (Enum 매핑)
    @Column(name = "salary_type")
    @Enumerated(EnumType.STRING)
    private SalaryType salaryType;

    @Column(name = "salary_amount")
    private Integer salaryAmount;

    @Column(name = "work_address")
    private String workAddress;

    private Double latitude;
    private Double longitude;

    // 🌟 [변경됨] String -> Enum (오타 방지 및 로직 처리를 위해 변경 추천)
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('RECRUITING', 'CLOSED') DEFAULT 'RECRUITING'")
    private JobStatus status;

    private LocalDate deadline;

    @Column(name = "view_count")
    private Integer viewCount;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // 기본값 자동 세팅
    @PrePersist
    public void prePersist() {
        if (this.viewCount == null)
            this.viewCount = 0;
        if (this.status == null)
            this.status = JobStatus.RECRUITING;
        if (this.salaryType == null)
            this.salaryType = SalaryType.HOURLY;
    }
}