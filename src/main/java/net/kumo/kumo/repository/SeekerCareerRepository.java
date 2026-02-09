package net.kumo.kumo.repository;

import net.kumo.kumo.domain.entity.SeekerCareerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeekerCareerRepository extends JpaRepository<SeekerCareerEntity, Long> {
}