package net.kumo.kumo.service;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.kumo.kumo.domain.dto.JoinRecruiterDTO;
import net.kumo.kumo.domain.entity.ProfileImageEntity;
import net.kumo.kumo.domain.entity.UserEntity;
import net.kumo.kumo.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class RecruiterService {

    private final UserRepository userRepository;

    /**
     * 유저 정보 불러오기
     * 
     * @param email
     * @return
     */
    public UserEntity getCurrentUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
    }

    /**
     * 유저의 프로필 이미지 경로를 업데이트합니다.
     * 
     * @param email     유저 식별용 이메일
     * @param imagePath 저장된 이미지의 웹 접근 경로
     */
    public void updateProfileImage(String email, String imagePath) {
        // 1. 이메일로 유저 정보를 가져옵니다.
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("해당 이메일을 가진 유저를 찾을 수 없습니다: " + email));

        ProfileImageEntity profileImageEntity = ProfileImageEntity.builder().fileUrl(imagePath).build();

        // 2. 새로운 이미지 경로를 세팅합니다. (엔티티의 setter 사용)
        user.setProfileImage(profileImageEntity);

        // 3. 변경 사항을 저장합니다.
        // @Transactional이 붙어있으면 사실 save를 안 호출해도 감지되어 업데이트되지만,
        // 명시적으로 적어주는 것이 가독성에 좋습니다.
        userRepository.save(user);
    }

    /**
     * 회원정보 수정
     * 
     * @param dto
     */
    public void updateProfile(JoinRecruiterDTO dto) {
        UserEntity user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("해당 이메일을 가진 유저를 찾을 수 없습니다: " + dto.getEmail()));

        // 2. 새 객체를 만들지 말고, 기존 객체의 알맹이(필드)만 쏙쏙 바꿔 입힙니다!
        // (UserEntity 클래스에 @Setter 나 수정용 메서드가 있어야 합니다.)
        user.setNickname(dto.getNickname());
        user.setZipCode(dto.getZipCode());
        user.setAddressMain(dto.getAddressMain());
        user.setAddressDetail(dto.getAddressDetail());
        user.setAddrPrefecture(dto.getAddrPrefecture());
        user.setAddrCity(dto.getAddrCity());
        user.setAddrTown(dto.getAddrTown());
        user.setLatitude(dto.getLatitude());
        user.setLongitude(dto.getLongitude());

        // 🌟 [최종 검문소] DB에 저장되기 직전, user 객체에 위도/경도가 잘 꽂혀있는지 확인!
        log.info("👉 DB 저장 직전 Entity 상태: 위도={}, 경도={}", user.getLatitude(), user.getLongitude());
    }

}
