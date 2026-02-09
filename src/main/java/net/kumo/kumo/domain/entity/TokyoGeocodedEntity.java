package net.kumo.kumo.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "tokyo_geocoded")
@Getter
@Setter
public class TokyoGeocodedEntity extends BaseEntity { // BaseEntity 와의 상속관계 설정 (중복 제거 및 코드 통합)

    // 도쿄 특화 필드 (테이블 구조에 맞게)

    @Column(nullable = false)
    private Double lat;

    @Column(nullable = false)
    private Double lng;

    @Column(name = "prefecture_jp")
    private String prefectureJp;

    @Column(name = "ward_city_jp") // 도쿄는 city/ward 통합
    private String wardCityJp;

    @Column(name = "prefecture_kr")
    private String prefectureKr;

    @Column(name = "ward_city_kr")
    private String wardCityKr;
}