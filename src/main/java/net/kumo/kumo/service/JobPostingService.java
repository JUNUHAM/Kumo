package net.kumo.kumo.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import net.kumo.kumo.domain.dto.JobPostFormDTO;
import net.kumo.kumo.domain.entity.CompanyEntity;
import net.kumo.kumo.domain.entity.JobPostingEntity;
import net.kumo.kumo.domain.entity.UserEntity;
import net.kumo.kumo.repository.CompanyRepository;
import net.kumo.kumo.repository.JobPostingRepository;
import net.kumo.kumo.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class JobPostingService {

    // 🌟 필요한 3가지 저장소를 모두 불러옵니다.
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final JobPostingRepository jobPostingRepository;

    @Transactional
    public void saveJobPost(JobPostFormDTO dto, String email) {

        // 1. 작성자(사장님) 정보 찾기
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("유저 정보를 찾을 수 없습니다."));

        // 2. 🌟 화면에서 선택한 '회사 정보' 찾기
        CompanyEntity company = null;
        if (dto.getCompanyId() != null) {
            company = companyRepository.findById(dto.getCompanyId())
                    .orElseThrow(() -> new IllegalArgumentException("선택한 회사를 찾을 수 없습니다."));
        }

        // 3. DTO의 데이터를 엔티티(DB용 객체)로 옮겨 담기
        JobPostingEntity jobPost = new JobPostingEntity();

        // 🌟 객체 통째로 연결 (ManyToOne 매핑)
        jobPost.setUser(user);
        jobPost.setCompany(company);

        // 기본 텍스트 정보 세팅
        jobPost.setTitle(dto.getTitle());
        jobPost.setPosition(dto.getPosition());

        // 💡 [센스 발휘] DB에 positionDetail 컬럼이 없어서, description 맨 위에 합쳐줍니다!
        StringBuilder finalDescription = new StringBuilder();
        if (dto.getPositionDetail() != null && !dto.getPositionDetail().isEmpty()) {
            finalDescription.append("[상세업무: ").append(dto.getPositionDetail()).append("]\n\n");
        }
        if (dto.getDescription() != null) {
            finalDescription.append(dto.getDescription());
        }
        jobPost.setDescription(finalDescription.toString());

        // 🌟 Enum 타입 세팅 (JobPostingEntity 안에 있는 Enum 사용)
        if (dto.getSalaryType() != null) {
            jobPost.setSalaryType(JobPostingEntity.SalaryType.valueOf(dto.getSalaryType()));
        }
        jobPost.setSalaryAmount(dto.getSalaryAmount());

        // 마감일
        jobPost.setDeadline(dto.getDeadline());

        // 🌟 초기 상태는 무조건 '모집중(RECRUITING)'
        jobPost.setStatus(JobPostingEntity.JobStatus.RECRUITING);

        // 4. DB에 최종 저장!
        JobPostingEntity savedJobPost = jobPostingRepository.save(jobPost);

        // 5. TODO: 이미지 파일 저장 로직
        // dto.getImages() 에 들어있는 파일들을 S3나 로컬 폴더에 저장하고
        // JobImageEntity를 만들어서 DB에 넣는 코드가 이 자리에 들어오면 완벽합니다!
    }
}