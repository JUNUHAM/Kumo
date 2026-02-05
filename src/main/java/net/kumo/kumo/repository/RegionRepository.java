package net.kumo.kumo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import net.kumo.kumo.domain.entity.Enum;
import net.kumo.kumo.domain.entity.RegionEntity;

public interface RegionRepository extends JpaRepository<RegionEntity, Long> {
	List<RegionEntity> findByRegionType(Enum.RegionType regionType);

	List<RegionEntity> findByParent_Id(Long parentId);
}