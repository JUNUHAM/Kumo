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
public class OsakaGeocodedEntity extends BaseEntity {

    @Column(nullable = false)
    private Double lat;

    @Column(nullable = false)
    private Double lng;

    private String prefectureJp;
    private String cityJp;
    private String wardJp;

    private String prefectureKr;
    private String cityKr;
    private String wardKr;
}