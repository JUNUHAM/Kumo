package net.kumo.kumo.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.kumo.kumo.domain.dto.JobManageListDTO;
import net.kumo.kumo.domain.dto.JobPostingRequestDTO;
import net.kumo.kumo.domain.entity.CompanyEntity;
import net.kumo.kumo.domain.entity.OsakaGeocodedEntity;
import net.kumo.kumo.domain.entity.TokyoGeocodedEntity; // 🌟 도쿄 엔티티 임포트 필요!
import net.kumo.kumo.domain.entity.UserEntity;
import net.kumo.kumo.domain.enums.JobStatus;
import net.kumo.kumo.repository.CompanyRepository;
import net.kumo.kumo.repository.OsakaGeocodedRepository;
import net.kumo.kumo.repository.TokyoGeocodedRepository; // 🌟 도쿄 레포지토리 임포트 필요!

@Service
@RequiredArgsConstructor
public class JobPostingService {

    private final OsakaGeocodedRepository osakaGeocodedRepository;
    private final TokyoGeocodedRepository tokyoGeocodedRepository; // 🌟 도쿄 레포지토리 추가
    private final CompanyRepository companyRepository;

    @Transactional
    public void saveJobPosting(JobPostingRequestDTO dto, List<MultipartFile> images, UserEntity user) {

        // 1. 단 한 번만! 회사 객체를 가져옵니다. (중복 조회 제거)
        CompanyEntity company = companyRepository.findById(dto.getCompanyId())
                .orElseThrow(() -> new IllegalArgumentException("회사를 찾을 수 없습니다. ID: " + dto.getCompanyId()));

        String companyName = company.getBizName();
        String address = (company.getAddressMain() != null ? company.getAddressMain() : "")
                + (company.getAddressDetail() != null ? " " + company.getAddressDetail() : "");
        Double lat = company.getLatitude() != null ? company.getLatitude().doubleValue() : 0.0;
        Double lng = company.getLongitude() != null ? company.getLongitude().doubleValue() : 0.0;

        String prefJp = company.getAddrPrefecture(); // 🌟 "東京都" 또는 "大阪府"
        String cityJp = company.getAddrCity();
        String wardJp = company.getAddrTown();

        // 2. 이미지 URL 처리
        String imgUrls = "";
        if (images != null && !images.isEmpty()) {
            imgUrls = images.stream()
                    .filter(f -> !f.isEmpty())
                    .map(f -> "/uploads/" + f.getOriginalFilename())
                    .collect(Collectors.joining(","));
        }

        // 급여 부분 임시 변수
        String salaryType;
        String salaryTypeJp;

        // 급여 기준 별 임시 변수 저장
        switch (dto.getSalaryType()) {
            case "HOURLY":
                salaryType = "시급";
                salaryTypeJp = "時給";
                break;

            case "DAILY":
                salaryType = "일급";
                salaryTypeJp = "日給";
                break;

            case "MONTHLY":
                salaryType = "월급";
                salaryTypeJp = "月給";
                break;

            case "SALARY":
                salaryType = "연봉";
                salaryTypeJp = "年収";
                break;

            default:
                salaryType = "미정";
                salaryTypeJp = "未定";
                break;
        }

        // 3. 급여 문자열 및 공통 데이터 세팅
        String wage = (dto.getSalaryType() != null && dto.getSalaryAmount() != null)
                ? salaryType + " " + dto.getSalaryAmount() + "엔"
                : "";

        String wageJp = (dto.getSalaryType() != null && dto.getSalaryAmount() != null)
                ? salaryTypeJp + " " + dto.getSalaryAmount() + "円"
                : "";

        long datanum = System.currentTimeMillis();
        LocalDateTime now = LocalDateTime.now();
        java.time.format.DateTimeFormatter writeTimeFormatter = java.time.format.DateTimeFormatter
                .ofPattern("yy.MM.dd");
        String writeTime = now.format(writeTimeFormatter);

        // 🌟🌟 4. [핵심] 도쿄 vs 오사카 분기 처리 🌟🌟
        if ("東京都".equals(prefJp)) {
            saveToTokyo(dto, user, company, companyName, address, lat, lng, prefJp, cityJp, wardJp, imgUrls, wage,
                    wageJp, datanum, now, writeTime);
        } else {
            // 기본값은 오사카로 처리 (大阪府이거나 다른 지역일 경우 일단 오사카 DB로)
            saveToOsaka(dto, user, company, companyName, address, lat, lng, prefJp, cityJp, wardJp, imgUrls, wage,
                    wageJp, datanum, now, writeTime);
        }
    }

    // ==========================================
    // 🚅 오사카 저장 로직 (기존 로직 분리)
    // ==========================================
    private void saveToOsaka(JobPostingRequestDTO dto, UserEntity user, CompanyEntity company, String companyName,
            String address, Double lat, Double lng, String prefJp, String cityJp, String wardJp, String imgUrls,
            String wage, String wageJp, long datanum, LocalDateTime now, String writeTime) {
        Integer maxNo = osakaGeocodedRepository.findMaxRowNo();
        Integer nextRowNo = (maxNo == null) ? 1 : maxNo + 1;

        OsakaGeocodedEntity entity = new OsakaGeocodedEntity();
        entity.setCreatedAt(now);
        entity.setWriteTime(writeTime);
        entity.setUser(user);
        entity.setCompanyName(companyName);
        entity.setCompany(company);
        entity.setAddress(address);
        entity.setLat(lat);
        entity.setLng(lng);
        entity.setPrefectureJp(prefJp);
        entity.setCityJp(cityJp);
        entity.setWardJp(wardJp);

        // 🌟 [추가] 수정 시 입력창에 다시 뿌려주기 위해 원본 데이터 저장!
        entity.setSalaryType(dto.getSalaryType()); // "HOURLY" 등 저장
        entity.setSalaryAmount(dto.getSalaryAmount()); // 1200 등 저장

        entity.setRowNo(nextRowNo);
        entity.setDatanum(datanum);
        entity.setTitle(dto.getTitle());
        entity.setContactPhone(dto.getContactPhone());
        entity.setHref("/Recruiter/posting/" + datanum);
        entity.setPosition(dto.getPosition());
        entity.setJobDescription(dto.getPositionDetail());
        entity.setBody(dto.getDescription());
        entity.setWage(wage);
        entity.setWageJp(wageJp);
        entity.setImgUrls(imgUrls.isEmpty() ? null : imgUrls);
        entity.setStatus(JobStatus.RECRUITING);

        parseAddressToSixColumnsOsaka(entity, address);
        osakaGeocodedRepository.save(entity);
    }

    // ==========================================
    // 🚅 도쿄 저장 로직 (신규 추가)
    // ==========================================
    private void saveToTokyo(JobPostingRequestDTO dto, UserEntity user, CompanyEntity company, String companyName,
            String address, Double lat, Double lng, String prefJp, String cityJp, String wardJp, String imgUrls,
            String wage, String wageJp, long datanum, LocalDateTime now, String writeTime) {
        Integer maxNo = tokyoGeocodedRepository.findMaxRowNo();
        Integer nextRowNo = (maxNo == null) ? 1 : maxNo + 1;

        TokyoGeocodedEntity entity = new TokyoGeocodedEntity();
        entity.setCreatedAt(now);
        entity.setWriteTime(writeTime);
        entity.setUser(user);
        entity.setCompanyName(companyName);
        entity.setCompany(company);
        entity.setAddress(address);
        entity.setLat(lat);
        entity.setLng(lng);
        entity.setPrefectureJp(prefJp);

        // 🌟 [추가] 수정 시 입력창에 다시 뿌려주기 위해 원본 데이터 저장!
        entity.setSalaryType(dto.getSalaryType()); // "HOURLY" 등 저장
        entity.setSalaryAmount(dto.getSalaryAmount()); // 1200 등 저장

        entity.setRowNo(nextRowNo);
        entity.setDatanum(datanum);
        entity.setTitle(dto.getTitle());
        entity.setContactPhone(dto.getContactPhone());
        entity.setHref("/Recruiter/posting/" + datanum);
        entity.setPosition(dto.getPosition());
        entity.setJobDescription(dto.getPositionDetail());
        entity.setBody(dto.getDescription());
        entity.setWage(wage);
        entity.setWageJp(wageJp);
        entity.setImgUrls(imgUrls.isEmpty() ? null : imgUrls);
        entity.setStatus(JobStatus.RECRUITING);

        parseAddressToSixColumnsTokyo(entity, address);
        tokyoGeocodedRepository.save(entity);
    }

    // ==========================================
    // 🗺️ 주소 파싱 로직 (오사카/도쿄 분리)
    // ==========================================
    private void parseAddressToSixColumnsOsaka(OsakaGeocodedEntity entity, String fullAddress) {
        // ... (사장님이 쓰시던 기존 parseAddressToSixColumns 코드와 동일하게 넣으시면 됩니다)
        if (fullAddress == null || fullAddress.isBlank())
            return;
        String[] parts = fullAddress.split("\\s+");
        String prefJp = null, cityJp = null, wardJp = null;

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

        if ("大阪府".equals(prefJp))
            entity.setPrefectureKr("오사카부");
        if ("大阪市".equals(cityJp))
            entity.setCityKr("오사카시");
        if (wardJp != null) {
            Map<String, String> wardMap = Map.of("中央区", "주오구", "浪速区", "나니와구", "北区", "기타구");
            entity.setWardKr(wardMap.getOrDefault(wardJp, wardJp));
        }
    }

    private void parseAddressToSixColumnsTokyo(TokyoGeocodedEntity entity, String fullAddress) {
        if (fullAddress == null || fullAddress.isBlank())
            return;
        String[] parts = fullAddress.split("\\s+");
        String prefJp = null, cityJp = null, wardJp = null;

        for (String part : parts) {
            if (part.endsWith("都"))
                prefJp = part; // 도쿄도는 府가 아니라 都입니다!
            else if (part.endsWith("市"))
                cityJp = part;
            else if (part.endsWith("区"))
                wardJp = part;
        }

        entity.setPrefectureJp(prefJp);
        // ✅ 수정 (도쿄 엔티티 구조에 맞게 통합!)
        // 도쿄는 시/구를 wardCityJp 하나로 쓰기로 했었죠!
        entity.setWardCityJp(wardJp != null ? wardJp : cityJp);

        if ("東京都".equals(prefJp))
            entity.setPrefectureKr("도쿄도");
        // 도쿄의 주요 구 번역 세팅
        // 2. 한국어 세팅 (setWardKr 대신 setWardCityKr 사용!)
        // 🗺️ 도쿄 23구 전체 번역 매핑 (Map.ofEntries 사용)
        if (wardJp != null) {
            Map<String, String> tokyoMap = Map.ofEntries(
                    Map.entry("千代田区", "지요다구"),
                    Map.entry("中央区", "주오구"),
                    Map.entry("港区", "미나토구"),
                    Map.entry("新宿区", "신주쿠구"),
                    Map.entry("文京区", "분쿄구"),
                    Map.entry("台東区", "다이토구"),
                    Map.entry("墨田区", "스미다구"),
                    Map.entry("江東区", "고토구"),
                    Map.entry("品川区", "시나가와구"),
                    Map.entry("目黒区", "메구로구"),
                    Map.entry("大田区", "오타구"),
                    Map.entry("世田谷区", "세타가야구"),
                    Map.entry("渋谷区", "시부야구"),
                    Map.entry("中野区", "나카노구"),
                    Map.entry("杉並区", "스기나미구"),
                    Map.entry("豊島区", "도시마구"),
                    Map.entry("北区", "기타구"),
                    Map.entry("荒川区", "아라카와구"),
                    Map.entry("板橋区", "이타바시구"),
                    Map.entry("練馬区", "네리마구"),
                    Map.entry("足立区", "아다치구"),
                    Map.entry("葛飾区", "가쓰시카구"),
                    Map.entry("江戸川区", "에도가와구"),
                    // 필요하다면 도쿄도의 주요 시(市)도 아래처럼 계속 추가할 수 있습니다!
                    Map.entry("八王子市", "하치오지시"),
                    Map.entry("町田市", "마치다시"));

            // 매핑된 한국어 구 이름이 있으면 넣고, 없으면 일본어 원본 그대로 저장!
            entity.setWardCityKr(tokyoMap.getOrDefault(wardJp, wardJp));
        }
    }

    /**
     * 특정 유저(이메일)의 도쿄 + 오사카 공고를 합쳐서 반환 (최신순 정렬)
     */
    public List<JobManageListDTO> getMyJobPostings(String email) {
        List<JobManageListDTO> result = new java.util.ArrayList<>();

        // 1. 오사카 공고 가져와서 바구니에 담기
        List<OsakaGeocodedEntity> osakaJobs = osakaGeocodedRepository.findByUser_Email(email);
        for (OsakaGeocodedEntity o : osakaJobs) {

            // 🌟 1. 여기서 영어를 한글로 싹 바꿔줍니다!
            String displayWage = o.getWage() != null ? o.getWage()
                    .replace("HOURLY", "시급")
                    .replace("DAILY", "일급")
                    .replace("MONTHLY", "월급")
                    .replace("SALARY", "연봉") : "";

            result.add(JobManageListDTO.builder()
                    .id(o.getId()) // 🌟 [추가] 오사카 테이블의 진짜 id
                    .datanum(o.getDatanum())
                    .title(o.getTitle())
                    .regionType("오사카") // 라벨링
                    .wage(displayWage)
                    .createdAt(o.getCreatedAt())
                    .status(o.getStatus() != null ? o.getStatus().name() : "RECRUITING")
                    .build());
        }

        // 2. 도쿄 공고 가져와서 바구니에 담기
        List<TokyoGeocodedEntity> tokyoJobs = tokyoGeocodedRepository.findByUser_Email(email);
        for (TokyoGeocodedEntity t : tokyoJobs) {

            // 🌟 1. 여기서 영어를 한글로 싹 바꿔줍니다!
            String displayWage = t.getWage() != null ? t.getWage()
                    .replace("HOURLY", "시급")
                    .replace("DAILY", "일급")
                    .replace("MONTHLY", "월급")
                    .replace("SALARY", "연봉") : "";

            result.add(JobManageListDTO.builder()
                    .id(t.getId()) // 🌟 [추가] 도쿄 테이블의 진짜 id
                    .datanum(t.getDatanum())
                    .title(t.getTitle())
                    .regionType("도쿄") // 라벨링
                    .wage(displayWage)
                    .createdAt(t.getCreatedAt())
                    .status(t.getStatus() != null ? t.getStatus().name() : "RECRUITING")
                    .build());
        }

        // 3. 🌟 두 리스트를 합친 후, 등록일(createdAt) 기준 '최신순(내림차순)' 정렬!
        result.sort((a, b) -> {
            if (a.getCreatedAt() == null)
                return 1;
            if (b.getCreatedAt() == null)
                return -1;
            return b.getCreatedAt().compareTo(a.getCreatedAt());
        });

        return result;
    }

    /**
     * 특정 유저의 공고 삭제 로직 (보안 검증 포함)
     */
    @Transactional
    public void deleteMyJobPosting(Long datanum, String region, String email) {
        if ("TOKYO".equalsIgnoreCase(region)) {
            // 1. 도쿄 공고 찾기
            TokyoGeocodedEntity entity = tokyoGeocodedRepository.findByDatanum(datanum)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 공고입니다."));

            // 2. 이 공고를 작성한 사람이 현재 접속한 사람(email)이 맞는지 확인!
            if (!entity.getUser().getEmail().equals(email)) {
                throw new IllegalStateException("삭제 권한이 없습니다.");
            }

            // 3. 삭제!
            tokyoGeocodedRepository.delete(entity);

        } else if ("OSAKA".equalsIgnoreCase(region)) {
            // 1. 오사카 공고 찾기
            OsakaGeocodedEntity entity = osakaGeocodedRepository.findByDatanum(datanum)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 공고입니다."));

            // 2. 권한 확인
            if (!entity.getUser().getEmail().equals(email)) {
                throw new IllegalStateException("삭제 권한이 없습니다.");
            }

            // 3. 삭제!
            osakaGeocodedRepository.delete(entity);
        } else {
            throw new IllegalArgumentException("알 수 없는 지역입니다.");
        }
    }
}