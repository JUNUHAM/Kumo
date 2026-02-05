package net.kumo.kumo.repository;


import net.kumo.kumo.domain.entity.SeekerEducationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeekerEducationRepository extends JpaRepository<SeekerEducationEntity, Long> {
	List<SeekerEducationEntity> findByUser_Id(Long userId);
}