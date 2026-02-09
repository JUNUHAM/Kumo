package net.kumo.kumo.repository;

import net.kumo.kumo.domain.entity.TokyoGeocodedEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TokyoNoGeocodedRepository extends JpaRepository<TokyoGeocodedEntity, Long> {
}