package net.kumo.kumo.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Formula;

@Entity
@Table(name = "tokyo_no_geocoded")
@Getter
@Setter
public class TokyoNoGeocodedEntity extends BaseEntity {

	// BaseEntity 와의 상속 관계 설정, 중복 코드 제거
	@Formula("NULL")
	private Double lat;

	@Formula("NULL")
	private Double lng;
}