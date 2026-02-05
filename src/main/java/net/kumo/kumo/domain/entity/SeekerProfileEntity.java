package net.kumo.kumo.domain.entity;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "seeker_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeekerProfileEntity {
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "seeker_profile_id")
	private Long id;
	
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false, unique = true)
	private UserEntity user;
	
	@Column(name = "self_pr", columnDefinition = "TEXT")
	private String selfPr;
	
	@Column(name = "is_public")
	private Boolean isPublic;
}