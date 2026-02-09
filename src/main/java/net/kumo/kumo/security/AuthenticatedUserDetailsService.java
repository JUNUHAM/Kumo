package net.kumo.kumo.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.kumo.kumo.domain.entity.UserEntity;
import net.kumo.kumo.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthenticatedUserDetailsService implements UserDetailsService {
	
	private final UserRepository ur;
	
	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		
		log.debug("로그인 시도 이메일 : {}", email);
		
		// 1. DB에서 회원 조회 (findByEmail 사용)
		UserEntity userEntity = ur.findByEmail(email)
				.orElseThrow(() -> new UsernameNotFoundException(email + " : 존재하지 않는 이메일입니다."));
		
		log.debug("조회된 회원 정보 : {}", userEntity);
		
		// 2. 인증 객체(AuthenticatedUser) 생성 (빌더 패턴)
		AuthenticatedUser user = AuthenticatedUser.builder()
				.email(userEntity.getEmail())          // 아이디 (이메일)
				.password(userEntity.getPassword())    // 암호화된 비밀번호
				.nameKanjiSei(userEntity.getNameKanjiSei()) // 성 (한자)
				.nameKanjiMei(userEntity.getNameKanjiMei()) // 이름 (한자)
				.nickname(userEntity.getNickname())    // 닉네임
				.role(userEntity.getRole().name())     // Role Enum -> String 변환 ("SEEKER")
				.enabled(userEntity.isActive())        // 계정 활성화 여부 (isActive -> enabled)
				.build();
		
		log.debug("생성된 인증 객체 : {}", user);
		
		return user;
	}
}