package net.kumo.kumo.domain.dto;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

import java.sql.Timestamp;

/**
 * 사용자가 즐겨찾기(찜)한 공고 게시글의 정보를 담고 있는 통합 DTO
 * (DB 매핑 및 프론트엔드 AJAX 통신 겸용)
 */
@Data
@NoArgsConstructor  // 🌟 필수: JSON 변환 및 MyBatis 매핑을 위한 기본 생성자
@AllArgsConstructor // 🌟 옵션: 모든 필드를 포함하는 생성자
public class ScrapDTO {
	
	// ===================================
	// 1. DB 테이블 매핑용 필드 (kumo.scraps)
	// ===================================
	private Long scrapId;
	private Long userId;
	private Long jobPostId;
	private Timestamp createTime;
	
	// ===================================
	// 2. 프론트엔드 통신용 추가 필드
	// ===================================
	private Long targetPostId;  // JS에서 AJAX로 보낼 때 받을 이름
	private boolean isScraped;  // JS로 응답(결과)을 돌려줄 때 쓸 이름
	
	private String targetSource;
	
}