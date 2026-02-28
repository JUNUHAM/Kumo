package net.kumo.kumo.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Formula;

@Entity
@Table(name = "osaka_no_geocoded")
@Getter
@Setter
public class OsakaNoGeocodedEntity extends BaseEntity {

	// 가상의 컬럼을 생성
	// DB에서 SELECT 할 때 "NULL AS lat" 처럼 동작하여 에러를 막고 null을 반환
	@Formula("NULL")
	private Double lat;

	@Formula("NULL")
	private Double lng;
}
