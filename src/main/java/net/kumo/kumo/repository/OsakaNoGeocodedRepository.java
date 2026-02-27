package net.kumo.kumo.repository;

import net.kumo.kumo.domain.entity.OsakaNoGeocodedEntity;
import org.springframework.stereotype.Repository;

@Repository
public interface OsakaNoGeocodedRepository extends BaseRepository<OsakaNoGeocodedEntity> {
	
	// 특별히 추가할 메소드 없음 (부모 기능만으로 충분)
}