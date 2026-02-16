package net.kumo.kumo.repository;

import net.kumo.kumo.domain.entity.Enum;
import net.kumo.kumo.domain.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
	
	// 이메일로 회원 찾기 (중복 검사 및 로그인용)
	Optional<UserEntity> findByEmail(String email);
	
	// 닉네임 중복 검사용
	boolean existsByNickname(String nickname);
	
	// 이메일 중복 검사용
	boolean existsByEmail(String email);
	
	@Query("SELECT u.email FROM UserEntity u " +
			"WHERE CONCAT(IFNULL(u.nameKanjiSei, ''), IFNULL(u.nameKanjiMei, '')) = :fullName " +
			"AND u.contact = :contact " +
			"AND u.role = :role")
	Optional<String> findEmailByKanjiNameAndContact(
			@Param("fullName") String fullName,
			@Param("contact") String contact,
			@Param("role") Enum.UserRole role
	);
	
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
	
	
}