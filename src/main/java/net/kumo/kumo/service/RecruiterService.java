package net.kumo.kumo.service;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.kumo.kumo.domain.dto.JoinRecruiterDTO;
import net.kumo.kumo.domain.entity.ProfileImageEntity;
import net.kumo.kumo.domain.entity.UserEntity;
import net.kumo.kumo.repository.ScheduleRepository;
import net.kumo.kumo.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class RecruiterService {

    private final UserRepository userRepository;
    private final ScheduleRepository scheduleRepository;

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
     * @param email            유저 식별용 이메일
     * @param imagePath        저장된 이미지의 웹 접근 경로
     * @param originalFileName 원본 파일명
     * @param storedFileName   UUID가 붙은 저장 파일명
     * @param fileSize         파일 크기
     */
    @org.springframework.transaction.annotation.Transactional
    public void updateProfileImage(String email, String imagePath, String originalFileName, String storedFileName,
            Long fileSize) {

        // 1. 이메일로 유저 정보를 가져옵니다.
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("해당 이메일을 가진 유저를 찾을 수 없습니다: " + email));

        // 🌟 2. 유저가 이미 가지고 있는 프로필 사진이 있는지 꺼내봅니다.
        ProfileImageEntity existingImage = user.getProfileImage();

        if (existingImage != null) {
            // 🟢 [Case A] 기존 프사가 있는 경우 -> 새로운 정보로 내용물만 덮어쓰기 (UPDATE)
            existingImage.setFileUrl(imagePath);
            existingImage.setOriginalFileName(originalFileName);
            existingImage.setStoredFileName(storedFileName);
            existingImage.setFileSize(fileSize);
        } else {
            // 🔵 [Case B] 기존 프사가 아예 없는 경우 -> 새로 만들어서 연결해주기 (INSERT)
            ProfileImageEntity newImage = ProfileImageEntity.builder()
                    .fileUrl(imagePath)
                    .originalFileName(originalFileName)
                    .storedFileName(storedFileName)
                    .fileSize(fileSize)
                    .user(user)
                    .build();
            user.setProfileImage(newImage);
        }

        // 3. 변경 사항을 저장합니다. (JPA의 더티 체킹 덕분에 알아서 UPDATE나 INSERT 쿼리가 나갑니다!)
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

    public void deleteSchedule(Long id) {
        scheduleRepository.deleteById(id);
    }

}
