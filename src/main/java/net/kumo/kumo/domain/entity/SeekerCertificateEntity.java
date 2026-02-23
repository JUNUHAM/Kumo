package net.kumo.kumo.domain.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "seeker_certificates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeekerCertificateEntity {
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "cert_id")
	private Long id;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private UserEntity user;
	
	@Column(name = "cert_name", length = 100)
	private String certName;
	
	@Column(name = "acquisition_date")
	private LocalDate acquisitionDate;
	
	@Column(length = 100)
	private String issuer;
}
