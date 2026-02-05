package net.kumo.kumo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import net.kumo.kumo.domain.entity.CompanyImageEntity;

public interface CompanyImageRepository extends JpaRepository<CompanyImageEntity, Long> {
	List<CompanyImageEntity> findByUser_Id(Long userId);
}