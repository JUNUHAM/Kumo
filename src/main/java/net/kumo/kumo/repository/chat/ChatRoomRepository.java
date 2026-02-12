package net.kumo.kumo.repository.chat;

import net.kumo.kumo.domain.entity.ChatRoomEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoomEntity, Long> {

    // ★ 수정됨: findBySeeker_Id -> findBySeeker_UserId
    // (UserEntity 안의 변수명이 userId라고 가정함)
    Optional<ChatRoomEntity> findBySeeker_UserIdAndRecruiter_UserId(Long seekerId, Long recruiterId);

    // ★ 수정됨: findBySeeker_Id -> findBySeeker_UserId
    List<ChatRoomEntity> findBySeeker_UserId(Long seekerId);

    // ★ 수정됨: findByRecruiter_Id -> findByRecruiter_UserId
    List<ChatRoomEntity> findByRecruiter_UserId(Long recruiterId);
}