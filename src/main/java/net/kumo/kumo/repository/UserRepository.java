package net.kumo.kumo.repository;

import net.kumo.kumo.domain.entity.Enum;
import net.kumo.kumo.domain.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    // [기존] 이메일로 회원 찾기
    Optional<UserEntity> findByEmail(String email);

    // [기존] 닉네임 중복 검사용
    boolean existsByNickname(String nickname);

    // [기존] 이메일 중복 검사용
    boolean existsByEmail(String email);

    // [기존] 이름과 연락처로 이메일 찾기
    @Query("SELECT u.email FROM UserEntity u " +
            "WHERE CONCAT(IFNULL(u.nameKanjiSei, ''), IFNULL(u.nameKanjiMei, '')) = :fullName " +
            "AND u.contact = :contact " +
            "AND u.role = :role")
    Optional<String> findEmailByKanjiNameAndContact(
            @Param("fullName") String fullName,
            @Param("contact") String contact,
            @Param("role") Enum.UserRole role
    );

    // [기존] 비밀번호 찾기 시 본인 확인용
    @Query("""
    SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END
    FROM UserEntity u
    WHERE u.email = :email
      AND CONCAT(IFNULL(u.nameKanjiSei, ''), IFNULL(u.nameKanjiMei, '')) = :fullName
      AND u.contact = :contact
      AND u.role = :role
""")
    boolean existsByEmailAndFullNameAndContactAndRole(
            @Param("email") String email,
            @Param("fullName") String fullName,
            @Param("contact") String contact,
            @Param("role") Enum.UserRole role
    );

    /* =========================================
       새로 추가된 통계용 쿼리 메소드
       ========================================= */

    /**
     * 특정 시간 이후 가입자 수 (신규 회원)
     */
    long countByCreatedAtAfter(LocalDateTime dateTime);

    /**
     * 활성화된 회원 수 (isActive = true)
     */
    long countByIsActiveTrue();

    /**
     * 비활성화된 회원 수 (isActive = false)
     */
    long countByIsActiveFalse();
}