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
public class TokyoGeocodedEntity extends BaseEntity {

    @Column(nullable = false)
    private Double lat;

    @Column(nullable = false)
    private Double lng;

    @Column(name = "prefecture_jp")
    private String prefectureJp;

    @Column(name = "ward_city_jp")
    private String wardCityJp;

    @Column(name = "prefecture_kr")
    private String prefectureKr;

    @Column(name = "ward_city_kr")
    private String wardCityKr;
}