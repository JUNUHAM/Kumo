package net.kumo.kumo.repository;

import jakarta.persistence.criteria.Predicate;
import net.kumo.kumo.domain.entity.BaseEntity;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA Specification을 활용하여 구인 공고(Job) 검색 조건을 동적으로 생성하는 도우미 클래스입니다.
 * 이 클래스를 사용하면 각 Repository 인터페이스마다 복잡한 @Query 문자열을 중복해서 작성할 필요 없이,
 * 자바 코드로 안전하고 유연하게 WHERE 조건절을 만들어 낼 수 있습니다.
 * * 4개로 쪼개진 테이블(도쿄/오사카, 위치O/위치X)에 대해 모두 동일한 검색 규칙을 적용하기 위해
 * 제네릭 타입 {@code <T extends BaseEntity>}을 사용하였습니다.
 */
public class JobSearchSpec {
	
	/**
	 * 사용자의 검색 조건(키워드, 지역)을 받아 JPA 쿼리 조건문(Specification)을 조립하여 반환합니다.
	 *
	 * @param keyword      사용자가 검색창에 입력한 키워드 (예: "알바", "서빙").
	 * 값이 존재할 경우 제목과 상호명(한국어/일본어) 필드에서 검색합니다.
	 * @param subRegion    사용자가 선택한 세부 지역 (예: "신주쿠구", "기타구").
	 * @param regionFields 엔티티마다 다르게 설정된 지역 컬럼의 이름들 (가변 인자).
	 *
	 * 위치 기반 도쿄: "wardCityJp", "wardCityKr"
	 * 위치 없는 테이블: "address"
	 *
	 * @param <T>          BaseEntity를 상속받는 모든 엔티티 타입
	 * @return             동적으로 생성된 JPA WHERE 조건절 (Specification 객체)
	 */
	public static <T extends BaseEntity> Specification<T> searchConditions(String keyword, String subRegion, String... regionFields) {
		
		// root: 검색할 엔티티 (예: TokyoGeocodedEntity)
		// query: 쿼리 자체에 대한 조작 (보통 잘 안 건드림)
		// cb (CriteriaBuilder): LIKE, EQUAL, OR, AND 같은 조건을 만들어주는 공구 상자
		return (root, query, cb) -> {
			
			// 조건을 담을 빈 바구니(리스트)를 준비합니다.
			List<Predicate> predicates = new ArrayList<>();
			
			// ==========================================
			// 1. 키워드 검색 로직 (OR 조건 묶음)
			// ==========================================
			// 사용자가 키워드를 입력했다면 (null이나 빈 칸이 아니라면)
			if (StringUtils.hasText(keyword)) {
				String pattern = "%" + keyword + "%"; // LIKE 검색을 위한 패턴 생성
				
				// 제목과 상호명(한국어/일본어) 중 하나라도 포함되면 OK라는 OR 조건을 바구니에 담습니다.
				predicates.add(cb.or(
						cb.like(root.get("title"), pattern),         // 한국어 제목
						cb.like(root.get("titleJp"), pattern),       // 일본어 제목
						cb.like(root.get("companyName"), pattern),   // 한국어 상호명
						cb.like(root.get("companyNameJp"), pattern)  // 일본어 상호명
				));
			}
			
			// ==========================================
			// 2. 세부 지역 필터링 로직 (OR 조건 묶음)
			// ==========================================
			// 사용자가 세부 지역을 선택했다면
			if (StringUtils.hasText(subRegion)) {
				List<Predicate> regionPreds = new ArrayList<>();
				
				// 넘겨받은 타겟 컬럼 이름들을 하나씩 돌면서 확인합니다.
				for (String field : regionFields) {
					// [수정] 모든 필드에 대해 'LIKE' 검색을 사용하여 공백/줄바꿈 이슈를 해결합니다!
					// address든 ward_kr이든 "%오타구%" 처럼 검색하게 됩니다.
					regionPreds.add(cb.like(root.get(field), "%" + subRegion + "%"));
				}
				
				// 타겟 컬럼들 중 하나라도 일치하면 OK라는 OR 조건을 바구니에 담습니다.
				predicates.add(cb.or(regionPreds.toArray(new Predicate[0])));
			}
			
			// ==========================================
			// 3. 최종 조립 및 반환 (AND 조건)
			// ==========================================
			// 바구니에 담긴 '키워드 조건'과 '지역 조건'을 모두 만족(AND)해야 하는 최종 조건을 반환합니다.
			// (만약 검색 조건이 아무것도 없다면, 빈 조건이 반환되어 전체 데이터를 조회합니다.)
			return cb.and(predicates.toArray(new Predicate[0]));
		};
	}
}