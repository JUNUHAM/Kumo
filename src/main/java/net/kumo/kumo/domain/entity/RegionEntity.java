package net.kumo.kumo.domain.entity;


import jakarta.persistence.*;
import lombok.*;
import net.kumo.kumo.domain.entity.Enum.RegionType;



@Entity
@Table(name = "regions")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class RegionEntity {
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "region_id")
	private Long id;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parent_id")
	private RegionEntity parent;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "region_type", nullable = false, length = 20)
	private RegionType regionType;
	
	@Column(name = "region_code", length = 20)
	private String regionCode;
	
	@Column(nullable = false, length = 100)
	private String name;
	
	@Column(name = "name_kana", length = 100)
	private String nameKana;
}
