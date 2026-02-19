package net.kumo.kumo.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import net.kumo.kumo.domain.entity.CompanyEntity;
import net.kumo.kumo.domain.entity.UserEntity;
import net.kumo.kumo.repository.CompanyRepository;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;

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
    @Transactional
    public void deleteCompany(Long id) {
        companyRepository.deleteById(id);
    }
}