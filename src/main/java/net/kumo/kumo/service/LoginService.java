package net.kumo.kumo.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.kumo.kumo.domain.dto.ChangeNewPWDTO;
import net.kumo.kumo.domain.dto.FindIdDTO;
import net.kumo.kumo.domain.dto.JoinRecruiterDTO;
import net.kumo.kumo.domain.dto.JoinSeekerDTO;
import net.kumo.kumo.domain.entity.Enum;
import net.kumo.kumo.domain.entity.EvidenceFileEntity;
import net.kumo.kumo.domain.entity.UserEntity;
import net.kumo.kumo.repository.EvidenceFileRepository;
import net.kumo.kumo.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class LoginService {
	
	private final UserRepository userRepository;
	private final EvidenceFileRepository fileRepository; // 파일 저장용 리포지토리
	private final PasswordEncoder passwordEncoder;
	
	
	public void joinRecruiter(JoinRecruiterDTO dto, List<String> savedFileNames) {// 1. 비밀번호 암호화
		
		
		// 2. DTO -> UserEntity 변환 (Builder 패턴 사용 권장)
		UserEntity e = UserEntity.builder()
				.email(dto.getEmail())
				.password(passwordEncoder.encode(dto.getPassword())) // 암호화된 비번 저장
				.nickname(dto.getNickname())
				// 이름 (한자/가나)
				.nameKanjiMei(dto.getNameKanjiMei())
				.nameKanjiSei(dto.getNameKanjiSei())
				.nameKanaMei(dto.getNameKanaMei())
				.nameKanaSei(dto.getNameKanaSei())
				.gender("M".equals(dto.getGender()) ? Enum.Gender.MALE : Enum.Gender.FEMALE )
				
				
				// 생년월일 (합쳐서 저장 예시)
				.birthDate(dto.getBirthDate())
				
				// 연락처
				.contact(dto.getContact())
				
				// 주소 및 좌표
				.zipCode(dto.getZipCode())
				.addressMain(dto.getAddressMain())
				.addressDetail(dto.getAddressDetail())
				.latitude(dto.getLatitude())
				.longitude(dto.getLongitude())
				
				// --- 주소 정보 (검색용 3단 분리) ---
				.addrPrefecture(dto.getAddrPrefecture())
				.addrCity(dto.getAddrCity())
				.addrTown(dto.getAddrTown())
				
				// 역할 (구인자) 및 가입경로
				.role(Enum.UserRole.RECRUITER)
				.joinPath(dto.getJoinPath())
				
				// 승인받아야지 true가능
				.adReceive(dto.isAdReceive())
				.isActive(false)
				.build();
		
		// 3. 회원 정보 DB 저장 (먼저 저장해야 ID가 생성됨)
		UserEntity savedUser = userRepository.save(e);
		
		// 4. 파일 정보 DB 저장 (1:N 관계)
		if (savedFileNames != null && !savedFileNames.isEmpty()) {
			for (String fileName : savedFileNames) {
				EvidenceFileEntity fileEntity = EvidenceFileEntity.builder()
						.fileName(fileName)
						.fileType("EVIDENCE") // 증빙서류라는 표시
						.user(savedUser)      // ★ 외래키 연결 (어떤 회원의 파일인지)
						.build();
				
				fileRepository.save(fileEntity);
			}
		}
	
	}
	
	// 구직자 db에 집어넣기
	public void insertSeeker(JoinSeekerDTO dto) {
	//role, birth, profileImage,password,gender
		
		UserEntity e= UserEntity.builder().
				role(Enum.UserRole.SEEKER).
				birthDate(dto.getBirthDate()).
				password(passwordEncoder.encode(dto.getPassword())).
				gender("M".equals(dto.getGender()) ? Enum.Gender.MALE : Enum.Gender.FEMALE ).
				email(dto.getEmail())
				.nickname(dto.getNickname())
				
				// --- 성명 정보 (4개) ---
				.nameKanjiSei(dto.getNameKanjiSei())
				.nameKanjiMei(dto.getNameKanjiMei())
				.nameKanaSei(dto.getNameKanaSei())
				.nameKanaMei(dto.getNameKanaMei())
				
				// --- 연락처 ---
				.contact(dto.getContact())
				
				// --- 주소 정보 (표시용) ---
				.zipCode(dto.getZipCode())
				.addressMain(dto.getAddressMain())
				.addressDetail(dto.getAddressDetail())
				
				// --- 주소 정보 (검색용 3단 분리) ---
				.addrPrefecture(dto.getAddrPrefecture())
				.addrCity(dto.getAddrCity())
				.addrTown(dto.getAddrTown())
				
				// --- 지도 좌표 ---
				.latitude(dto.getLatitude())
				.longitude(dto.getLongitude())
				
				// --- 가입 및 설정 정보 ---
				.joinPath(dto.getJoinPath())
				.adReceive(dto.isAdReceive()) // lombok이라서 getAdReceive가 아니라 isAdReceive
				.isActive(true)               // 가입 즉시 활성화
				.build();
		
				userRepository.save(e);
	}
	
	
	// 이메일 중복성체크
	public boolean existsByEmail(String email) {
		boolean exists = userRepository.existsByEmail(email);
		return exists;
	}
	
	// 닉네임 중복성체크
	public boolean existsByNickname(String nickname) {
		boolean exists = userRepository.existsByNickname(nickname);
		return exists;
	}
	
	//이메일 연락처 이름 role 전부 맞을때 true,false 반환
	public boolean  emailVerify(String name, String contact, String email, String role) {

		Enum.UserRole userRole = Enum.UserRole.valueOf(role);

		String cleanName = name.replace(" ", "").replace("　", ""); // 반각, 전각 공백 모두 제거


		return userRepository.existsByEmailAndFullNameAndContactAndRole(
				email,
				cleanName,
				contact,
				userRole
		);
	}
	
	
	
	public String findId(FindIdDTO dto) {
		log.info("LoginService.findId 호출됨: {}", dto);
		// 1. 이름 공백 제거 (사용자가 "田中 太郎"라고 쳤어도 "田中太郎"로 변환)
		String cleanName = dto.getName().replace(" ", "").replace("　", ""); // 반각, 전각 공백 모두 제거
		
		// 2. 연락처 하이픈
		String cleanContact = dto.getContact().trim();
		
		// 3. String Role -> Enum Role 변환
		// HTML에서 "SEEKER" 또는 "RECRUITER"로 넘어옴
		Enum.UserRole role;
		try {
			role = Enum.UserRole.valueOf(dto.getRole());
		} catch (IllegalArgumentException | NullPointerException e) {
			log.error("잘못된 역할(Role) 값이 넘어왔습니다: {}", dto.getRole());
			return null; // 역할이 이상하면 못 찾은 것으로 처리
		}
		
		log.info("아이디 찾기 시도 -> 이름: {}, 연락처: {}, 역할: {}", cleanName, cleanContact, role);
		
		
		
		// 4. DB 조회 및 결과 반환 (없으면 null 반환)
		return userRepository.findEmailByKanjiNameAndContact(cleanName, cleanContact, role)
				.orElse(null);
		
	}
	
	@Transactional
	public void ChangeNewPW(ChangeNewPWDTO ChangeNewPWDTO) {
		
		
		// 이메일로 유저 엔티티 찾기
		UserEntity entity = userRepository.findByEmail(ChangeNewPWDTO.getEmail()).orElseThrow(()->new IllegalArgumentException("존재하지않은 회원"));
		
		// 비밀번호 암호화
		String encodedPassWord = passwordEncoder.encode(ChangeNewPWDTO.getPassword());
		
		// 엔티티 비밀번호 변경
		entity.setPassword(encodedPassWord);
		
		
	
	}
}
