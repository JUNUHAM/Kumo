package net.kumo.kumo.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FindIdDTO {
	private String name;        // 사용자가 입력한 이름 (성+이름 붙여서 올 것임)
	private String contact;     //
	private String role;    // "SEEKER" 또는 "RECRUITER"
}
