package net.kumo.kumo.repository;

import net.kumo.kumo.domain.entity.OsakaNoGeocodedEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OsakaNoGeocodedRepository extends JpaRepository<OsakaNoGeocodedEntity, Long> {
	Optional<OsakaNoGeocodedEntity> findByDatanum(String datanum);
	
}