package net.kumo.kumo.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import net.kumo.kumo.domain.entity.CompanyEntity;
import net.kumo.kumo.domain.entity.UserEntity;
import net.kumo.kumo.repository.CompanyRepository;
import net.kumo.kumo.repository.OsakaGeocodedRepository;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final OsakaGeocodedRepository osakaGeocodedRepository;

    // 회사 목록 조회
    public List<CompanyEntity> getCompanyList(UserEntity user) {
        return companyRepository.findAllByUser(user);
    }

    // 단일 회사 조회
    public CompanyEntity getCompany(Long id) {
        return companyRepository.findById(id).orElse(null);
    }

    // 저장 및 수정
    @Transactional
    public void saveCompany(CompanyEntity company, UserEntity user) {
        company.setUser(user); // 어떤 유저의 회사인지 연결
        companyRepository.save(company);
    }

    // 삭제
    public void deleteCompany(Long companyId) {
        long count = osakaGeocodedRepository.countByCompany_CompanyId(companyId);
        if (count > 0) {
            throw new IllegalStateException(
                    "이 회사를 참조하는 공고가 " + count + "개 있어 삭제할 수 없습니다.");
        }
        companyRepository.deleteById(companyId);
    }
}