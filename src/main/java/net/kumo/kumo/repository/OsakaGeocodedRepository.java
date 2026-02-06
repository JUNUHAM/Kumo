package net.kumo.kumo.repository;

import net.kumo.kumo.domain.entity.OsakaGeocodedEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OsakaGeocodedRepository extends JpaRepository<OsakaGeocodedEntity, Long> {
	Optional<OsakaGeocodedEntity> findByDatanum(String datanum);
}