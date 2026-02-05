package net.kumo.kumo.repository;

import net.kumo.kumo.domain.entity.SeekerLanguageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeekerLanguageRepository extends JpaRepository<SeekerLanguageEntity, Long> {
	List<SeekerLanguageEntity> findByUser_Id(Long userId);
}