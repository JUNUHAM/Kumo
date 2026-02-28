package net.kumo.kumo.domain.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "schedules")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long scheduleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user; // 일정 주인

    @Column(nullable = false)
    private String title; // 주제

    @Column(columnDefinition = "TEXT")
    private String description; // 상세정보

    @Column(nullable = false, name = "start_at")
    private LocalDateTime startAt; // 시작 일시

    @Column(nullable = false, name = "end_at")
    private LocalDateTime endAt; // 종료 일시

    @Column(name = "color_code")
    private String colorCode; // 이미지의 색상 버튼에서 넘어온 값 (예: #ff6b6b)

    // 생성/수정 시간 관리는 별도의 BaseEntity가 없다면 여기에 추가하거나
    // @EnableJpaAuditing 설정을 통해 관리하는 것이 좋습니다.
}