package net.kumo.kumo.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.kumo.kumo.domain.dto.JobPostingRequestDTO;
import net.kumo.kumo.domain.entity.CompanyEntity;
import net.kumo.kumo.domain.entity.OsakaGeocodedEntity;
import net.kumo.kumo.domain.entity.UserEntity;
import net.kumo.kumo.domain.enums.JobStatus;
import net.kumo.kumo.repository.CompanyRepository;
import net.kumo.kumo.repository.OsakaGeocodedRepository;

@Service
@RequiredArgsConstructor
public class JobPostingService {

    private final OsakaGeocodedRepository osakaGeocodedRepository;
    private final CompanyRepository companyRepository;

    /**
     * 공고 등록 (companies 테이블 연동 및 OsakaGeocoded 테이블 통합 저장)
     */
    @Transactional
    public void saveJobPosting(JobPostingRequestDTO dto, List<MultipartFile> images, UserEntity user) {

        // 1. [연동 핵심] 선택한 회사 정보 및 위치 정보 추출
        CompanyEntity company = null;
        String companyName = null;
        String address = null;
        Double lat = 0.0;
        Double lng = 0.0;

        // 지역 필드 (지도 필터 및 차트용)
        String prefJp = null;
        String cityJp = null;
        String wardJp = null;

        if (dto.getCompanyId() != null) {
            company = companyRepository.findById(dto.getCompanyId())
                    .orElseThrow(() -> new IllegalArgumentException("선택한 회사가 존재하지 않습니다."));

            companyName = company.getBizName();
            // 주소 결합 (메인 + 상세)
            address = (company.getAddressMain() != null ? company.getAddressMain() : "")
                    + (company.getAddressDetail() != null ? " " + company.getAddressDetail() : "");

            // 위도/경도 변환
            if (company.getLatitude() != null)
                lat = company.getLatitude().doubleValue();
            if (company.getLongitude() != null)
                lng = company.getLongitude().doubleValue();

            // 🌟 [추가] 지역구 정보 연동 (지도 검색 및 도넛 차트 동기화)
            prefJp = company.getAddrPrefecture();
            cityJp = company.getAddrCity();
            wardJp = company.getAddrTown();

            // 회사 아이디 저장
            company.setCompanyId(dto.getCompanyId());
        }

        // 2. 이미지 URL 처리
        String imgUrls = "";
        if (images != null && !images.isEmpty()) {
            imgUrls = images.stream()
                    .filter(f -> !f.isEmpty())
                    .map(f -> "/uploads/" + f.getOriginalFilename())
                    .collect(Collectors.joining(","));
        }

        // 3. 급여 문자열 조합
        String wage = "";
        if (dto.getSalaryType() != null && dto.getSalaryAmount() != null) {
            wage = dto.getSalaryType() + " " + dto.getSalaryAmount() + "円";
        }

        // 4. row_no 번호 자동 생성 (Integer 타입 대응)
        Integer maxNo = osakaGeocodedRepository.findMaxRowNo();
        Integer nextRowNo = (maxNo == null) ? 1 : maxNo + 1;

        // 5. datanum 생성 (고유 식별자)
        long datanum = System.currentTimeMillis();

        // 6. 엔티티 생성 및 데이터 매핑
        OsakaGeocodedEntity entity = new OsakaGeocodedEntity();

        // 1. 날짜 포맷터 준비 (YY.MM.DD 형식)
        java.time.format.DateTimeFormatter writeTimeFormatter = java.time.format.DateTimeFormatter
                .ofPattern("yy.MM.dd");

        // 2. 현재 시간을 기준으로 생성 시간 세팅
        LocalDateTime now = LocalDateTime.now();
        entity.setCreatedAt(now);

        // 🌟 [핵심] 첫 번째 사진(write_time)을 두 번째 사진(created_at)에서 추출하여 채우기
        // 2026-02-24 -> 26.02.24 로 변환됩니다.
        entity.setWriteTime(now.format(writeTimeFormatter));

        // 유저 정보 저장
        entity.setUser(user);

        // 🌟 연관 관계 및 지역 데이터 세팅
        entity.setCompanyName(companyName);
        entity.setAddress(address);
        entity.setLat(lat);
        entity.setLng(lng);
        entity.setPrefectureJp(prefJp);
        entity.setCityJp(cityJp);
        entity.setWardJp(wardJp);

        // 공고 기본 정보 세팅
        entity.setRowNo(nextRowNo);
        entity.setDatanum(datanum);
        entity.setTitle(dto.getTitle());
        entity.setContactPhone(dto.getContactPhone());
        entity.setHref("/Recruiter/posting/" + datanum);
        entity.setPosition(dto.getPosition());
        entity.setJobDescription(dto.getPositionDetail());
        entity.setBody(dto.getDescription());
        entity.setWage(wage);
        entity.setImgUrls(imgUrls.isEmpty() ? null : imgUrls);

        // 상태값
        entity.setCreatedAt(LocalDateTime.now());
        entity.setStatus(JobStatus.RECRUITING);

        // 전체 주소 쪼개기 및 저정
        parseAddressToSixColumns(entity, entity.getAddress());

        // 7. 저장
        osakaGeocodedRepository.save(entity);
    }

    /**
     * 일본어 전체 주소에서 현, 시, 구를 추출하고 한국어로 번역하여 세팅
     */
    private void parseAddressToSixColumns(OsakaGeocodedEntity entity, String fullAddress) {
        if (fullAddress == null || fullAddress.isBlank())
            return;

        // 1. 일본어 주소 추출 (JP)
        // 예: "大阪府 大阪市 東淀川구..." -> " ", "府", "市", "区" 기준으로 파싱
        String[] parts = fullAddress.split("\\s+"); // 공백 기준으로 분리

        String prefJp = null;
        String cityJp = null;
        String wardJp = null;

        for (String part : parts) {
            if (part.endsWith("府") || part.endsWith("県"))
                prefJp = part;
            else if (part.endsWith("市"))
                cityJp = part;
            else if (part.endsWith("区"))
                wardJp = part;
        }

        entity.setPrefectureJp(prefJp);
        entity.setCityJp(cityJp);
        entity.setWardJp(wardJp);

        // 2. 한국어 번역 매핑 (KR) - 오사카 기준 전용 매핑
        if ("大阪府".equals(prefJp))
            entity.setPrefectureKr("오사카부");
        if ("大阪市".equals(cityJp))
            entity.setCityKr("오사카시");

        if (wardJp != null) {
            // 이미지(image_4414b1.jpg)에 등장하는 주요 구 매핑
            Map<String, String> wardMap = Map.of(
                    "中央区", "주오구",
                    "浪速区", "나니와구",
                    "北区", "기타구",
                    "福島区", "후쿠시마구",
                    "都島구", "미야코지마구",
                    "大正区", "다이쇼구",
                    "東淀川区", "히가시요도가와구");
            entity.setWardKr(wardMap.getOrDefault(wardJp, wardJp)); // 매핑 없으면 일본어 그대로 유지
        }
    }
}