package net.kumo.kumo.repository;

import net.kumo.kumo.domain.entity.ChatMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessageEntity, Long> {
	List<ChatMessageEntity> findByRoom_IdOrderByCreatedAtAsc(Long roomId);
	long countByRoom_IdAndIsReadFalse(Long roomId);
}