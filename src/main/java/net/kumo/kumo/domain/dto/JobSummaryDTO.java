package net.kumo.kumo.domain.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import net.kumo.kumo.domain.dto.projection.JobSummaryView;
import net.kumo.kumo.domain.entity.BaseEntity;
import net.kumo.kumo.domain.entity.OsakaGeocodedEntity;
import net.kumo.kumo.domain.entity.TokyoGeocodedEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@NoArgsConstructor
public class JobSummaryDTO {

    private Long id;
    private String source;
    private String title;
    private String status;

    // ★ 필수 필드 확인
    private String position;
    private LocalDate deadline;

    private String companyName;
    private String address;
    private String wage;
    private String contactPhone;
    private String thumbnailUrl;
    private String writeTime;
    private LocalDateTime createdAt;
    private Double lat;
    private Double lng;
    
    // ==========================================
    // 🌟 [핵심 추가] 작성자 ID와 진짜 이름(닉네임)을 담을 그릇
    // ==========================================
    private Long userId;
    private String managerName;

    // 1. View 생성자 (바텀 시트용)
    public JobSummaryDTO(JobSummaryView view, String lang, String source) {
        this.id = view.getId();
        this.source = source;
        this.thumbnailUrl = view.getThumbnailUrl();
        this.contactPhone = view.getContactPhone();
        this.address = view.getAddress();
        this.writeTime = view.getWriteTime();
        this.lat = view.getLat();
        this.lng = view.getLng();
        this.deadline = null; // 크롤링 데이터는 마감일 없음
        
        this.userId = view.getUserId();
        this.managerName = view.getManagerName();

        boolean isJp = "ja".equalsIgnoreCase(lang);
        this.title = (isJp && hasText(view.getTitleJp())) ? view.getTitleJp() : view.getTitle();
        this.companyName = (isJp && hasText(view.getCompanyNameJp())) ? view.getCompanyNameJp() : view.getCompanyName();
        this.wage = (isJp && hasText(view.getWageJp())) ? view.getWageJp() : view.getWage();

        // 직무 필드 (없으면 회사명으로 대체)
        this.position = view.getCompanyName();
    }

    // 2. Entity 생성자 (일반 리스트 반환용)
    public JobSummaryDTO(BaseEntity entity, String lang, String source) {
        this.id = entity.getId();
        this.source = source;
        this.thumbnailUrl = entity.getImgUrls();
        this.contactPhone = entity.getContactPhone();
        this.address = entity.getAddress();
        this.userId = entity.getUserId();

        // DB에 status가 없거나(null), 아직 구현 안 된 경우 기본값 'RECRUITING' 적용
        if (entity.getStatus() != null) {
            this.status = entity.getStatus().name(); // Enum일 경우 .name()
        } else {
            this.status = "RECRUITING"; // ★ DB가 비어있으면 승인됨으로 간주
        }

        if (entity.getWriteTime() != null) {
            this.writeTime = entity.getWriteTime();
        } else if (entity.getCreatedAt() != null) {
            this.writeTime = entity.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }
        this.createdAt = entity.getCreatedAt();
        this.deadline = null;
        
        // 🌟 좌표 및 실제 유저 이름(닉네임) 추출
        if (entity instanceof OsakaGeocodedEntity osaka) {
	        this.lat = osaka.getLat();
            this.lng = osaka.getLng();
            if (osaka.getUser() != null) {
                this.managerName = osaka.getUser().getNickname();
            }
        } else if (entity instanceof TokyoGeocodedEntity tokyo) {
	        this.lat = tokyo.getLat();
            this.lng = tokyo.getLng();
            if (tokyo.getUser() != null) {
                this.managerName = tokyo.getUser().getNickname();
            }
        } else {
            this.lat = null;
            this.lng = null;
        }

        boolean isJp = "ja".equalsIgnoreCase(lang);
        this.title = (isJp && hasText(entity.getTitleJp())) ? entity.getTitleJp() : entity.getTitle();
        this.companyName = (isJp && hasText(entity.getCompanyNameJp())) ? entity.getCompanyNameJp() : entity.getCompanyName();
        this.wage = (isJp && hasText(entity.getWageJp())) ? entity.getWageJp() : entity.getWage();

        // ★ 직무 필드 매핑
        this.position = (isJp && hasText(entity.getPositionJp())) ? entity.getPositionJp() : entity.getPosition();
    }

    private boolean hasText(String str) {
        return str != null && !str.isBlank();
    }
}