package net.kumo.kumo.repository;

import net.kumo.kumo.domain.entity.Enum;
import net.kumo.kumo.domain.entity.RegionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


public interface RegionRepository extends JpaRepository<RegionEntity, Long> {
}