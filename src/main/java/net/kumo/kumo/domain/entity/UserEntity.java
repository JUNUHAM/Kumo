package net.kumo.kumo.domain.entity;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import net.kumo.kumo.domain.entity.Enum.Gender;
import net.kumo.kumo.domain.entity.Enum.UserRole;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEntity {
	
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "user_id")
	private Long id;
	
	@Column(nullable = false, unique = true, length = 100)
	private String email;
	
	@Column(length = 255)
	private String password;
	
	@Column(name = "name_kanji_sei", length = 50)
	private String nameKanjiSei;
	
	@Column(name = "name_kanji_mei", length = 50)
	private String nameKanjiMei;
	
	@Column(name = "name_kana_sei", length = 50)
	private String nameKanaSei;
	
	@Column(name = "name_kana_mei", length = 50)
	private String nameKanaMei;
	
	@Column(length = 50)
	private String nickname;
	
	@Column(length = 100)
	private String contact;
	
	@Enumerated(EnumType.STRING)
	@Column(length = 20)
	private Gender gender;
	
	@Enumerated(EnumType.STRING)
	@Column(length = 20)
	private UserRole role;
	
	@Column(name = "social_provider", length = 30)
	private String socialProvider;
	
	@Column(name = "social_id", length = 100)
	private String socialId;
	
	@Column(name = "profile_image", length = 500)
	private String profileImage;
	
	@Column(name = "birth_date")
	private LocalDate birthDate;
	
	@Column(name = "is_active")
	private Boolean isActive;
	
	@Column(name = "ad_receive")
	private Boolean adReceive;
	
	@CreationTimestamp
	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;
	
	@UpdateTimestamp
	@Column(name = "updated_at")
	private LocalDateTime updatedAt;
}