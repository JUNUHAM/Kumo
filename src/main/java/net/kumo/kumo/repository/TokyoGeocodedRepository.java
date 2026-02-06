package net.kumo.kumo.repository;

import net.kumo.kumo.domain.entity.TokyoGeocodedEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TokyoGeocodedRepository extends JpaRepository<TokyoGeocodedEntity, Long> {
	Optional<TokyoGeocodedEntity> findByDatanum(String datanum);
}
