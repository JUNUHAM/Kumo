package net.kumo.kumo.domain.dto;

import lombok.*;
import net.kumo.kumo.domain.entity.Enum;
import net.kumo.kumo.domain.entity.UserEntity;

import java.time.LocalDate;

/**
 * 구직자(Seeker) 마이페이지의 개인 프로필 및 계정 정보를
 * 화면에 렌더링하기 위해 사용되는 DTO 클래스입니다.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class SeekerMyPageDTO {

    private Long id;
    private String email;
    private String name;
    private String nickname;

    /** 등록된 프로필 이미지 접근 URL */
    private String fileUrl;

    private String address;
    private LocalDate birthDate;
    private String contact;

    /** 소셜 로그인 연동 제공자 정보 */
    private Enum.SocialProvider SocialProvider;

    /**
     * UserEntity 객체를 기반으로 화면 렌더링용 SeekerMyPageDTO를 생성합니다.
     *
     * @param userEntity 변환할 구직자 엔티티 객체
     * @return 매핑이 완료된 SeekerMyPageDTO 객체
     */
    public static SeekerMyPageDTO EntityToDto(UserEntity userEntity) {
        return SeekerMyPageDTO.builder()
                .id(userEntity.getUserId())
                .email(userEntity.getEmail())
                .contact(userEntity.getContact())
                .name(userEntity.getNameKanjiSei() + " " + userEntity.getNameKanjiSei())
                .nickname(userEntity.getNickname())
                .fileUrl(userEntity.getProfileImage() != null ? userEntity.getProfileImage().getFileUrl() : null)
                .address(userEntity.getAddressMain() + " " + userEntity.getAddressDetail())
                .birthDate(userEntity.getBirthDate())
                .SocialProvider(userEntity.getSocialProvider())
                .build();
    }
}