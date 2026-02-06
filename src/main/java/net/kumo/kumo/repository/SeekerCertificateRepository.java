package net.kumo.kumo.repository;

import net.kumo.kumo.domain.entity.SeekerCertificateEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeekerCertificateRepository extends JpaRepository<SeekerCertificateEntity, Long> {
	List<SeekerCertificateEntity> findByUser_Id(Long userId);
}