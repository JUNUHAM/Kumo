package net.kumo.kumo.domain.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "companies")
@Data
public class CompanyEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long companyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user; // 사장님(리크루터)과 연결

    private String bizName;
    private String ceoName;
    private String zipCode;
    private String addressMain;
    private String addressDetail;
    private String addrPrefecture;
    private String addrCity;
    private String addrTown;

    // 위도: 전체 10자리 중 소수점 아래 8자리
    @Column(precision = 10, scale = 8)
    private BigDecimal latitude;

    // 경도: 전체 11자리 중 소수점 아래 8자리
    @Column(precision = 11, scale = 8)
    private BigDecimal longitude;

    @Column(columnDefinition = "TEXT")
    private String introduction;
}
