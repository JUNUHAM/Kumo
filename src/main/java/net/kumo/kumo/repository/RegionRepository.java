package net.kumo.kumo.repository;

import net.kumo.kumo.domain.entity.Enum;
import net.kumo.kumo.domain.entity.RegionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RegionRepository extends JpaRepository<RegionEntity, Long> {
	List<RegionEntity> findByRegionType(Enum.RegionType regionType);

	List<RegionEntity> findByParent_Id(Long parentId);
}