package net.kumo.kumo.repository;

import net.kumo.kumo.domain.entity.SeekerProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SeekerProfileRepository extends JpaRepository<SeekerProfileEntity, Long> {
	Optional<SeekerProfileEntity> findByUser_Id(Long userId);
	boolean existsByUser_Id(Long userId);
}