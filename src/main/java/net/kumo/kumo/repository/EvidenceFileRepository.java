package net.kumo.kumo.repository;

import net.kumo.kumo.domain.entity.EvidenceFileEntity;
import net.kumo.kumo.domain.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EvidenceFileRepository extends JpaRepository<EvidenceFileEntity, Long> {
	//유저랑 파일 가져오기
	List<EvidenceFileEntity> findByUserAndFileType(UserEntity user, String fileType);
	
}