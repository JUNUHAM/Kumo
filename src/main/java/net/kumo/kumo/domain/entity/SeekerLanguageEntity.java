package net.kumo.kumo.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "seeker_languages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeekerLanguageEntity {
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "lang_id")
	private Long id;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private UserEntity user;
	
	@Column(length = 50)
	private String language;
	
	@Column(length = 50)
	private String level;
}