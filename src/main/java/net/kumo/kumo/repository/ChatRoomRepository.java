package net.kumo.kumo.repository;

import net.kumo.kumo.domain.entity.ChatRoomEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoomEntity, Long> {
	List<ChatRoomEntity> findBySeeker_Id(Long seekerId);
	List<ChatRoomEntity> findByRecruiter_Id(Long recruiterId);
	
	Optional<ChatRoomEntity> findByJobPosting_IdAndSeeker_Id(Long jobPostId, Long seekerId);
	boolean existsByJobPosting_IdAndSeeker_Id(Long jobPostId, Long seekerId);
}