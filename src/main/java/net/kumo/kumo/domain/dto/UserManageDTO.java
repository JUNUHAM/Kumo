package net.kumo.kumo.domain.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import net.kumo.kumo.domain.entity.UserEntity;
import java.time.format.DateTimeFormatter;

@Getter
@NoArgsConstructor
public class UserManageDTO {
    private Long id;              // user_id
    private String email;
    private String nickname;
    private String name;          // 이름 (한자 성+이름)
    private String role;          // SEEKER, RECRUITER, ADMIN
    private String status;        // ACTIVE, INACTIVE (isActive 기반)
    private String profileImage;  // 프로필 이미지 경로
    private String joinedAt;      // 가입일
    private String lastActive;    // 마지막 활동 (updatedAt 사용)

    public UserManageDTO(UserEntity user) {
        this.id = user.getUserId();
        this.email = user.getEmail();
        this.nickname = user.getNickname();

        // 성명 합치기 (한자 성 + 이름)
        this.name = user.getNameKanjiSei() + " " + user.getNameKanjiMei();

        // Role 매핑 (Enum -> String)
        if (user.getRole() != null) {
            this.role = user.getRole().name();
        } else {
            this.role = "SEEKER";
        }

        // Status 매핑 (boolean isActive -> String)
        this.status = user.isActive() ? "ACTIVE" : "INACTIVE";

        // 프로필 이미지 (없으면 기본값)
        this.profileImage = user.getProfileImage();
        if (this.profileImage == null || this.profileImage.isBlank()) {
            this.profileImage = "/images/default_profile.png";
        }

        // 날짜 포맷팅
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");

        if (user.getCreatedAt() != null) {
            this.joinedAt = user.getCreatedAt().format(formatter);
        } else {
            this.joinedAt = "-";
        }

        // 마지막 활동 (Last Fail 혹은 UpdatedAt 등을 활용, 여기선 UpdatedAt 사용)
        if (user.getUpdatedAt() != null) {
            this.lastActive = user.getUpdatedAt().format(formatter);
        } else {
            this.lastActive = "-";
        }
    }
}