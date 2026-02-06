package net.kumo.kumo.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "osaka_geocoded")
@Getter
@Setter
public class OsakaGeocodedEntity extends BaseEntity { // BaseEntity 와의 상속관계 설정 (중복 제거 및 코드 통합)

    @Column(nullable = false)
    private Double lat;

    @Column(nullable = false)
    private Double lng;

    @Column(name = "prefecture_jp")
    private String prefectureJp;

    @Column(name = "city_jp")
    private String cityJp;

    @Column(name = "ward_jp")
    private String wardJp;

    @Column(name = "prefecture_kr")
    private String prefectureKr;

    @Column(name = "city_kr")
    private String cityKr;

    @Column(name = "ward_kr")
    private String wardKr;
}