package net.kumo.kumo.domain.entity;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "seeker_educations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeekerEducationEntity {
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "edu_id")
	private Long id;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private UserEntity user;
	
	@Column(name = "school_name", length = 100)
	private String schoolName;
	
	@Column(length = 100)
	private String major;
	
	@Column(length = 50)
	private String status;
}
